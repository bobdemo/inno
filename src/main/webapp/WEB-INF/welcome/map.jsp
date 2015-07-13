<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <t:layout>
    <jsp:body>
    <div class="span12">
            <h3>Mappa di prova per lo strato informativo ${layername}</h3>
            <i><strong>Nota: la mappa  visualizza solo la prima pagina dei dati per ciascuna tile</strong> </i>
            <br /><br /> <table class="table table-striped">
            <tbody>
            <c:forEach items="${info}" var="item">
            <tr>
            <td><strong>${item.key}</strong></td><td>${item.value}</td>
            </tr>
            </c:forEach>
            </tbody>
            </table>
            <br/>
            <div class="myContent" id="map" style="padding: 0; margin: 0; width: 100%; height: 600px;"></div>
            <script>
                var layername = "${layername}";
                var bbox = "${info._bbox_}"; 
                
            </script>
            <script src="/${bucket}/resources/js/inno.js"></script>
            <script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>
    </jsp:body>
</t:layout>