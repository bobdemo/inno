<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <t:layout>
    <jsp:body>
    <div class="span12">
            <h3>Web App di accesso ai dati di INNO</h3>
            <p>Lista Strati informativi presenti: </p>
            <table class="table table-striped">
            <tr>
            <th><strong>Nome strato</strong></th><th><strong>Tipo geometrie</strong></th><th></th>
            </tr>
            <tbody>
            <c:forEach items="${info}" var="item">
            <tr>
            <td>${item.key}</td><td>${item.value}</td><td><a href='/<c:out value="${bucket}" />/html/map/<c:out value="${item.key}" />' >Vai alla mappa</a></td>
            </tr>
            </c:forEach>
            </tbody>
            </table>
            <br/><br/>
            <h4>La web app espone i seguenti metodi per accedere ai documenti JSON:</h4>
            <table class="table" >
<tr class="success"><td>
<strong>Lista dei strati informativi:</strong></td><td>"<em>/<c:out value="${bucket}" />/json/list(/&lt;page&gt;)</em>"</td>
</tr>
<tr class="active">
<td><strong>Geometrie degli elementi di uno strato in un tassello:</strong></td><td>"<em>/<c:out value="${bucket}" />/json/tile/&lt;nome_strato&gt;/&lt;x&gt;/&lt;y&gt;/&lt;zoom&gt;(/&lt;page&gt;)</em>"</td>
</tr>
<tr class="success">
<td><strong>Informazioni su di uno strato informativo: </strong></td><td>"<em>/<c:out value="${bucket}" />/json/layer/&lt;nome_strato&gt;</em>"</td>
</tr>
<tr class="error">
<td><strong>Dati alfanumerici delle feature di uno strato in un tassello: </strong></td><td>"<em>/<c:out value="${bucket}" />/json/infos/&lt;nome_strato&gt;/&lt;x&gt;/&lt;y&gt;/&lt;zoom&gt;(/&lt;page&gt;)</em>"</td>
</tr>
<tr class="success">
<td><strong>Singolo campo alfanumerico delle feature di uno strato in un tassello:</strong></td><td>"<em>/<c:out value="${bucket}" />/json/value/&lt;nome_strato&gt;/&lt;nome_attributo&gt;/&lt;x&gt;/&lt;y&gt;/&lt;zoom&gt;(/&lt;page&gt;)</em>"</td>
</tr>
<tr class="active">
<td><strong>Dati alfanumerici di una feature di uno strato</strong></td><td>"<em>/<c:out value="${bucket}" />/json/feature/&lt;layer_name&gt;/&lt;feature-ID&gt;</em>"</td>
</tr>
<tr class="success">
<td><strong>Mappa di test per uno strato</strong></td><td>"<em>/<c:out value="${bucket}" />/html/map/&lt;layer_name&gt;</em>"</td>
</tr>
</table>
            <hr />
<p>dove: <ul><li> <i>x</i>,<i>y</i> e <i>zoom</i> sono le coordinate della tile secondo la specifica <a href="http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">Slippy map tilenames</a></li>
<li> <i>nome_strato</i> &egrave il nome dello strato informativo</li>
<li> <i>feature-id</i> &egrave l'identificativo dell'elemento nello strato informativo</li>
<li> <i>page (opzionale)</i> &egrave un intero maggiore di zero che indica la pagina dei dati del tassello (ogni pagina ha dimensioni minori di 25Kb)</li></ul>

</p>

    </div>
    </jsp:body>
</t:layout>
 