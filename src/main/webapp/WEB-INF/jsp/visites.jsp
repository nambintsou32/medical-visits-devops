<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Visites  Gestion des visites mdicales</title>
</head>
<body>
  <main>
    <p>
      <a href="${pageContext.request.contextPath}/">Accueil</a>
       <a href="${pageContext.request.contextPath}/patients">Patients</a>
       <a href="${pageContext.request.contextPath}/medecins">Mdecins</a>
    </p>

    <h1>Visites mdicales</h1>

    <c:if test="${param.message eq 'create'}">
      <p><strong>Visite cre avec succs.</strong></p>
    </c:if>
    <c:if test="${param.message eq 'replace'}">
      <p><strong>Visite modifie avec succs.</strong></p>
    </c:if>
    <c:if test="${param.message eq 'delete'}">
      <p><strong>Visite supprime avec succs.</strong></p>
    </c:if>
    <c:if test="${not empty error}">
      <p><strong>Erreur : <c:out value="${error}"/></strong></p>
    </c:if>

    <p>
      <a href="${pageContext.request.contextPath}/visites/new">
        Ajouter une visite
      </a>
    </p>

    <c:choose>
      <c:when test="${empty visites}">
        <p>Aucune visite enregistre.</p>
      </c:when>
      <c:otherwise>
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Mdecin</th>
              <th>Patient</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach items="${visites}" var="visite">
              <tr>
                <td><c:out value="${visite.visitDate}"/></td>
                <td>
                  <c:out value="${visite.medecin.codeMed}"/>
                  
                  <c:out value="${visite.medecin.nom}"/>
                  <c:out value="${visite.medecin.prenom}"/>
                </td>
                <td>
                  <c:out value="${visite.patient.codePat}"/>
                  
                  <c:out value="${visite.patient.nom}"/>
                  <c:out value="${visite.patient.prenom}"/>
                </td>
                <td>
                  <a href="${pageContext.request.contextPath}/visites/edit?codeMed=${visite.id.codeMed}&amp;codePat=${visite.id.codePat}&amp;visitDate=${visite.id.visitDate}">
                    Modifier
                  </a>

                  <form method="post"
                        action="${pageContext.request.contextPath}/visites"
                        style="display: inline;">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden"
                           name="codeMed"
                           value="<c:out value="${visite.id.codeMed}"/>">
                    <input type="hidden"
                           name="codePat"
                           value="<c:out value="${visite.id.codePat}"/>">
                    <input type="hidden"
                           name="visitDate"
                           value="<c:out value="${visite.id.visitDate}"/>">
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
