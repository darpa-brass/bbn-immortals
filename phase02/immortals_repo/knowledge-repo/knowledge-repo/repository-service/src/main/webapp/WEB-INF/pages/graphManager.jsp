<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>

<body>
	<h2>Graph management interface</h2>

	<table>
	   <tr>
            <td>
                <form
                    action="<c:url value='/immortalsRepositoryServiceUi/'/>"
                    method="post">
                    <input type="submit" name="refresh_button"
                        value="refresh" /> <input
                        type="hidden" name="_method" value="POST">
                </form>
            </td>
        </tr>
        
        <tr>
            <td>
                <form
                    action="<c:url value='/immortalsRepositoryService/bootstrap'/>"
                    method="post">
                    <input type="submit" name="refresh_button"
                        value="bootstrap" /> <input
                        type="hidden" name="_method" value="POST">
                </form>
            </td>
        </tr>
	
	   <tr></tr><tr></tr><tr></tr><tr></tr><tr></tr><tr></tr><tr></tr><tr></tr>
	   
		<c:forEach var="graphUri" items="${graphs}">
			<tr>
				<td>${graphUri}</td>
				<td>
				
				<form action="<c:url value='/immortalsRepositoryService/graph'/>" method="get">
                        <input type="submit" name="get_button" value="view"/> 
                        <input type="hidden" name="_method" value="GET">
                        <input type="hidden" name="graphUri" value="${fn:escapeXml(graphUri)}" />
                    </form>
				
				</td>
				<td>
					<form action="<c:url value='/immortalsRepositoryService/graph'/>" method="post">
						<input type="submit" name="delete_button" value="delete"/> 
						<input type="hidden" name="_method" value="DELETE">
						<input type="hidden" name="graphUri" value="${fn:escapeXml(graphUri)}" />
					</form>
				</td>
				
			</tr>
		</c:forEach>
		
		<tr></tr><tr></tr><tr></tr><tr></tr><tr></tr><tr></tr><tr></tr><tr></tr>
		
		<tr>
			<td>
				<form
					action="<c:url value='/immortalsRepositoryService/zeroizeFuseki'/>"
					method="post">
					<input type="submit" name="delete_button"
						value="ZEROIZE Fuseki (warning: deletes all graphs in /ds)" /> <input
						type="hidden" name="_method" value="DELETE">
				</form>
			</td>
		</tr>
	</table>
</body>
</html>