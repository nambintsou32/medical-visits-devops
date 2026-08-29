<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Patients  Gestion des visites mdicales</title>
</head>
<body>
  <main>
    <p>
      <a href="${pageContext.request.contextPath}/">Accueil</a>
       <a href="${pageContext.request.contextPath}/medecins">Mdecins</a>
       <a href="${pageContext.request.contextPath}/visites">Visites</a>
    </p>

    <h1>Patients</h1>

    <c:if test="${param.message eq 'create'}">
      <p><strong>Patient cr avec succs.</strong></p>
    </c:if>
    <c:if test="${param.message eq 'update'}">
      <p><strong>Patient modifi avec succs.</strong></p>
    </c:if>
    <c:if test="${param.message eq 'delete'}">
      <p><strong>Patient supprim avec succs.</strong></p>
    </c:if>

    <p>
      <a href="${pageContext.request.contextPath}/patients/new">
        Ajouter un patient
      </a>
    </p>

    <form method="get"
          action="${pageContext.request.contextPath}/patients">
      <label for="search">Rechercher :</label>
      <input id="search"
             name="search"
             value="<c:out value="${search}"/>">

      <label for="searchBy">par</label>
      <select id="searchBy" name="searchBy">
        <option value="nom"
          <c:if test="${searchBy eq 'nom'}">selected</c:if>>
          nom
        </option>
        <option value="code"
          <c:if test="${searchBy eq 'code'}">selected</c:if>>
          code patient
        </option>
      </select>

      <button type="submit">Rechercher</button>
      <a href="${pageContext.request.contextPath}/patients">Rinitialiser</a>
    </form>

    <c:choose>
      <c:when test="${empty patients}">
        <p>Aucun patient trouv.</p>
      </c:when>
      <c:otherwise>
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Nom</th>
              <th>Prnom</th>
              <th>Sexe</th>
              <th>Adresse</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${patients}" var="patient">
              <tr>
                <td><c:out value="${patient.codePat}"/></td>
                <td><c:out value="${patient.nom}"/></td>
                <td><c:out value="${patient.prenom}"/></td>
                <td><c:out value="${patient.sexe}"/></td>
                <td><c:out value="${patient.adresse}"/></td>
                <td>
                  <a href="${pageContext.request.contextPath}/patients/edit?codePat=${patient.codePat}">
                    Modifier
                  </a>

                  <form method="post"
                        action="${pageContext.request.contextPath}/patients"
                        style="display: inline;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden"
                           name="codePat"
                           value="<c:out value="${patient.codePat}"/>">
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
