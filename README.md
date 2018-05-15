# Gateway网关原型设计

### 技术选型
Java8; Spring Boot; Jersey(Restful的参考实现); Spring cloud; JWT; MySQL；Guava Cache

### 规范
- 需要转发的URL全部小写。一般格式是 `/gw/服务名/具体路径...`。服务名全局唯一。
- 支持Restful和SOAP两种请求的转发。
- SOAP请求的判断标志：HTTP Header中contentType包含“text/xml”或者“application/soap+xml”且methond为post
  因为SOAP 1.2 uses content type "application/soap+xml" and SOAP 1.1 uses "text/xml"
- HTTP数据用UTF-8编码

### 处理流程和表设计
1. 客户端用clientId和密码换得Token。token中加密保存了clientId和过期时间。如果希望token长期有效只需把过期时间设置为很长时间之后。例如10万小时后，约等于11年后过期。

2. 以后客户端的请求中一律在http header中包含这个token作为自己身份的标示。token的生成解析代码已包含，加密token的证书请每个项目自行生成。
示例数据：
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiJjbGllbnQwMDEiLCJpc3MiOiJnYXRld2F5IiwiZXhwIjoxNTIyNzc2NjMzfQ.CF_TC6kmv1BeLbWM_oPMweCbZi3wdLveKCS42UzZxa6PDkP1j4htwF-7exIuzylLetPCUZG7Ri1tZQ8QcuwZBW8WbpHPnEdzmP6yGcrW9ykd9LrdX1HbWx7iZ82-I9xHxzA1pzEVcj_3gJzinPTohwCKtusDnWBz4zvAderoIl0XaXJ4ynKNTTqAkhMnl1GGEGWbbpy3c-nLRnw5GxFmYdMSozy8691BOjkcYGMykyXa0RwzDIhHq5VfMpM0xQW_BvomGJYrIqz3zceU37Cxk1Yxfvk3GY_AamZ10oDUZQ0lkjMgEYbtInAlReHCpCXMgbQlGWPmHS3Z8b9JW_Gzrw

3. Gateway的path和后端服务的真正地址的映射关系存在数据库中。 service_route示例数据：

| id  | enabled | path        | retryable | service_name | strip_prefix | url                   |
|-----|---------|-------------|-----------|--------------|--------------|-----------------------|
| 1   | 1       | /gw/svc1/** | 1         | svc1         | 1            | http://{svc1_ip}:port |
| 2   | 1       | /gw/soap/** | 1         | soap         | 1            | http://{soap_ip}:port |

假设本地8809端口启动了gateway，针对 http://localhost:8809/gw/svc1/item/list/ 的请求会被转发到 http://{svc1_ip}:port/item/list/


4. 后端的服务的权限粒度具体到某URL的某个HTTP Method。gateway_policy示例数据：

| id  | http_method | role      | service_name | url         |
|-----|-------------|-----------|--------------|-------------|
| 1   | GET         | DEVELOPER | svc1         | /gw/svc1/** |
| 2   | POST        | DEVELOPER | soap         | /gw/soap/** |

第一行表示有一个名为svc1的后端服务，需要客户端具备的role是DEVELOPER才可通过GET请求访问/gw/svc1/**
特殊role ANONYMOUS：表示任何客户端都可访问此http_method+URL。

5. ​客户端对后端服务的特定URL，HTTP Method设置角色。client_roles示例数据： gateway_policy_id就是gateway_policy表的主键

| id  | client_id | role      | gateway_policy_id |
|-----|-----------|-----------|-------------------|
| 1   | client001 | DEVELOPER | 1                 |
| 2   | client001 | DEVELOPER | 2                 |

6. 关于缓存
Gateway的path和后端服务地址的映射关系自动缓存，如需要刷新可模仿ServiceRouteEndpoint
权限相关数据是否缓存由配置security.cache-enabled决定。如启动缓存需在任何改动权限数据的操作后重新加载数据，参考GatewayPolicyEndpoint和ClientRolesEndpoint

7. 路径的匹配采用Spring AntPathMatcher
通配符：
- "?" matches one character
- "*" matches zero or more characters
- "**" matches zero or more directories in a path
具体例子参考AntPathMatcher中类的注释

### API 一览
gateway.endpoint下所有Endpoing结尾的class


### 参考资料
- Spring Boot官方文档 https://www.gitbook.com/book/qbgbook/spring-boot-reference-guide-zh/details
- Spring Cloud Zuul http://www.ymq.io/2017/12/11/spring-cloud-zuul-filter/
- JPA 关联查询 https://www.jianshu.com/p/cc4e199cbb14

