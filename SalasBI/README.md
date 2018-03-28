# SalasBI

沙拉, 健康, 色彩丰富, 充满活力.

### Sources
　　收集数据的服务模块, 无论是日志文件, http接口, mysql, mongodb, 都有相应的数据收集策略.<br/>

　　数据格式将支持**json**, **protobuf**以及**自定义格式**.<br/>

　　多个**Monitor**互不影响, 数据由**Trimmer**进行

　　<b>RESTFul API设计; </b>

 > GET (select): 从服务器取出资源 (一项或多项) <br/>
 > POST (create): 在服务器新建一个资源 <br/>
 > PUT (update): 在服务器更新资源 (客户端提供改变后的完整资源) <br/>
 > PATCH (update): 在服务器更新资源 (客户端提供改变的属性) <br/>
 > DELETE (delete): 从服务器删除资源<br/>
 > 
 > HEAD：获取资源的元数据 <br/>
 > OPTIONS：获取信息，关于资源的哪些属性是客户端可以改变的 <br/>
