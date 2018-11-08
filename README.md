## API服务网关设计
服务网关将系统中国对外暴露的服务聚合起来，所有要调用这些服务的客户端都需要通过网关进行访问，基于这种方式网关可以对API进行统一管控，例如：认证、鉴权、流量控制、监控等等。

本平台网关基于[spring cloud zuul](http://www.ymq.io/2017/12/11/spring-cloud-zuul-filter/) + Spring boot 1.5.X实现。

### 处理流程
1. 客户端用clientId和密码login，网关将请求转发给“认证授权服务”验证账号。
成功后网关生成Token。token中加密保存了clientId和token过期时间。
基于Token认证的好处如下：
服务端无状态：Token 机制在服务端不需要存储 session 信息，因为 Token 自身包含了所有用户的相关信息。
性能较好，因为在验证 Token 时不用再去访问数据库或者远程服务进行权限校验，可以提升性能。


2. 以后客户的请求中一律在http header中包含这个token作为自己身份的标示。网关会解析检查token的值是否合法，是否过期等，解析出clientId放入http header。后端服务即可直接取得clientId
token示例数据：
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiJjbGllbnQwMDEiLCJpc3MiOiJnYXRld2F5IiwiZXhwIjoxNTIyNzc2NjMzfQ.CF_TC6kmv1BeLbWM_oPMweCbZi3wdLveKCS42UzZxa6PDkP1j4htwF-7exIuzylLetPCUZG7Ri1tZQ8QcuwZBW8WbpHPnEdzmP6yGcrW9ykd9LrdX1HbWx7iZ82-I9xHxzA1pzEVcj_3gJzinPTohwCKtusDnWBz4zvAderoIl0XaXJ4ynKNTTqAkhMnl1GGEGWbbpy3c-nLRnw5GxFmYdMSozy8691BOjkcYGMykyXa0RwzDIhHq5VfMpM0xQW_BvomGJYrIqz3zceU37Cxk1Yxfvk3GY_AamZ10oDUZQ0lkjMgEYbtInAlReHCpCXMgbQlGWPmHS3Z8b9JW_Gzrw

### 数据库表结构
1. Gateway中的path和后端服务的真正地址的映射关系存在数据库表 service_route中。示范数据：

| id  | enabled | path        | retryable | service_name | strip_prefix | url                   |
|-----|---------|-------------|-----------|--------------|--------------|-----------------------|
| 1   | 1       | /gw/svc1/** | 1         | svc1         | 1            | http://{svc1_name}:port |
| 2   | 1       | /gw/svc2/** | 1         | svc2         | 1            | http://{svc2_name}:port |

需要转发的URL全部小写。一般格式是 `/gw/服务名/具体路径...`。服务名全局唯一。
假设本地8809端口启动了gateway，针对 http://localhost:8809/gw/svc1/item/list/ 的请求会被转发到 http://{svc1_name}:port/item/list/


2. 数据库表 gateway_policy 存放后端服务需要什么样的权限才能访问，权限粒度具体到某URL的某个HTTP Method。示范数据：

| id  | http_method | role      | service_name | url         |
|-----|-------------|-----------|--------------|-------------|
| 1   | GET         | DEVELOPER | svc1         | /gw/svc1/** |
| 2   | GET         | DEVELOPER | svc2         | /gw/svc2/** |
| 2   | POST        | ADMIN     | svc2         | /gw/svc2/** |

第一行表示有一个名为svc1的后端服务，需要客户端具备的role是DEVELOPER才可通过GET请求访问/gw/svc1/**
通过合理安排url的路径可以达到灵活设置服务内子功能权限的目的。例如：
/gw/svc1/func1/**
/gw/svc1/func2/**
**注意要避免一个http请求符合多条权限记录中的url。**

特殊role ANONYMOUS：表示任何客户端（包括没登录过的）都可访问此http_method+URL。

3. client_roles表存放客户端对后端服务的特定URL，HTTP Method具备什么样的角色（拥有什么样的权限）。示例数据： 
字段gateway_policy_id就是前面gateway_policy表的主键

| id  | client_id | role      | gateway_policy_id |
|-----|-----------|-----------|-------------------|
| 1   | client001 | DEVELOPER | 1                 |
| 2   | client001 | DEVELOPER | 2                 |

###  关于缓存
Gateway的path和后端服务地址的映射关系自动缓存，如需要刷新可模仿ServiceRouteEndpoint
权限相关数据是否缓存由配置security.cache-enabled决定。如启动缓存需在任何改动权限数据的操作后重新加载数据，参考GatewayPolicyEndpoint和ClientRolesEndpoint

### 其他
####  路径的匹配采用Spring AntPathMatcher
通配符：
- "?" matches one character
- "*" matches zero or more characters
- "**" matches zero or more directories in a path
具体例子参考AntPathMatcher中类的注释

####  URL路径说明
- /user-service/?   它可以匹配/user-service/之后拼接一个任务字符的路径，比如：/user-service/a、/user-service/b、/user-service/c
- /user-service/*   它可以匹配/user-service/之后拼接任意字符的路径，比如：/user-service/a、/user-service/aaa、/user-service/bbb。但是它无法匹配/user-service/a/b
- /user-service/** 它可以匹配/user-service/*包含的内容之外，还可以匹配形如/user-service/a/b的多级目录路径
具体例子参考AntPathMatcher中类的注释

####   动态加载
由于网关服务担负外部访问统一入口的任务，它可能需要动态更新内部逻辑的能力，比如动态修改路由规则。

####   JWT 刷新方案
一种常见的做法是增加一个refreshToken（原来的token称为 accessToken）
例如accessToken有效时间15分钟，refreshToken的有效时间30分钟，当前端使用accessToken发请求时发现过期则用refreshToken重新获取一套新的token，包含一套新的accessToken和refreshToken。
也就是refreshToken的有效时间才是真正的JWT有效时间。

还有一种做法是，前端有一个独立线程，每隔10分钟重新获得一次新的token。

### API 一览
参考所有Endpoing结尾的class

### 参考资料
- Spring Boot官方文档 https://www.gitbook.com/book/qbgbook/spring-boot-reference-guide-zh/details
- Spring Cloud Zuul http://www.ymq.io/2017/12/11/spring-cloud-zuul-filter/
- JPA 关联查询 https://www.jianshu.com/p/cc4e199cbb14
- 如何处理jwt token超时后的刷新 https://stackoverflow.com/questions/26739167/jwt-json-web-token-automatic-prolongation-of-expiration?rq=1

