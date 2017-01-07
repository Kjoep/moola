<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html xm>
    <head><title>Transactions for ${account.name}</title></head>
    <body>
        <table>

        <c:forEach items="#{transactions}" var="transaction">
            <tr>
                <td>${transaction.description}</td>
                <td>${transaction.amount}</td>
            </tr>

        </c:forEach>

        </table>
    </body>

</html>