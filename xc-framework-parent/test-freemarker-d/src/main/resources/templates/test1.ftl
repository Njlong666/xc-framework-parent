<!DOCTYPE html>
<html>
<head><meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
</body>
<br/>
<table>

    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>金额</td>
        <td>出生日期</td>
    </tr>
    <#if stus?? >
    <#list stus as stu>
        <tr>
            <td>${stu_index}</td>
            <td <#if stu.name=='小明'>style="background: cornflowerblue;" </#if>>${stu.name}</td>
            <td>${stu.age}</td>
           <#-- <td>${stu.money}</td>-->
            <#--  <td>${stu.birthday}</td> -->
        </tr>
    </#list>
    </#if>
</table>
遍历数据模型的stuMap

<br/>
姓名:${stuMap['stu1'].name}<br/>
年龄:${stuMap['stu1'].age}<br/>
<br/>
<#list stuMap?keys as k>
    姓名:${stuMap[k].name}<br/>
    年龄:${stuMap[k].age}<br/>
</#list>
<br/>
</html>