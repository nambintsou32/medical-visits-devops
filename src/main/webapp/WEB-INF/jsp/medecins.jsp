<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Mdecins  Gestion des visites mdicales</title>
</head>
<body>
  <main>
    <p>
      <a href="${pageContext.request.contextPath}/">Accueil</a>
       <a href="${pageContext.request.contextPath}/patients">Patients</a>
       <a href="${pageContext.request.contextPath}/visites">Visites</a>
    </p>

    <h1>Mdecins</h1>

    <c:if test="${param.message eq 'create'}">
      <p><strong>Mdecin cr avec succs.</strong></p>
    </c:if>
    <c:if test="${param.message eq 'update'}">
      <p><strong>Mdecin modifi avec succs.</strong></p>
    </c:if>
    <c:if test="${param.message eq 'delete'}">
      <p><strong>Mdecin supprim avec succs.</strong></p>
    </c:if>
    <c:if test="${not empty error}">
      <p><strong>Erreur : <c:out value="${error}"/></strong></p>
    </c:if>

    <p>
      <a href="${pageContext.request.contextPath}/medecins/new">
        Ajouter un mdecin
      </a>
    </p>

    <c:choose>
      <c:when test="${empty medecins}">
        <p>Aucun mdecin enregistr.</p>
      </c:when>
      <c:otherwise>
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Nom</th>
              <th>Prnom</th>
              <th>Grade</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${medecins}" var="medecin">
              <tr>
                <td><c:out value="${medecin.codeMed}"/></td>
                <td><c:out value="${medecin.nom}"/></td>
                <td><c:out value="${medecin.prenom}"/></td>
                <td><c:out value="${medecin.grade}"/></td>
                <td>
                  <a href="${pageContext.request.contextPath}/medecins/edit?codeMed=${medecin.codeMed}">
                    Modifier
                  </a>

                  <form method="post"
                        action="${pageContext.request.contextPath}/medecins"
                        style="display: inline;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden"
                           name="codeMed"
                           value="<c:out value="${medecin.codeMed}"/>">
                    <button type="submit">Supprimer</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </main>
</body>
</html>
