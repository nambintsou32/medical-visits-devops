<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Visite  Gestion des visites mdicales</title>
</head>
<body>
  <main>
    <p>
      <a href="${pageContext.request.contextPath}/visites">
        Retour  la liste des visites
      </a>
    </p>

    <c:choose>
      <c:when test="${empty visiter}">
        <h1>Ajouter une visite</h1>
      </c:when>
      <c:otherwise>
        <h1>Modifier une visite</h1>
      </c:otherwise>
    </c:choose>

    <c:if test="${not empty error}">
      <p><strong>Erreur : <c:out value="${error}"/></strong></p>
    </c:if>

    <c:if test="${empty medecins or empty patients}">
      <p>
        Il faut crer au moins un mdecin et un patient avant
        denregistrer une visite.
      </p>
    </c:if>

    <c:if test="${not empty medecins and not empty patients}">
      <form method="post"
            action="${pageContext.request.contextPath}/visites">
        <c:choose>
          <c:when test="${empty visiter}">
            <input type="hidden" name="action" value="create">
          </c:when>
          <c:otherwise>
            <input type="hidden" name="action" value="replace">
            <input type="hidden"
                   name="currentCodeMed"
                   value="<c:out value="${visiter.id.codeMed}"/>">
            <input type="hidden"
                   name="currentCodePat"
                   value="<c:out value="${visiter.id.codePat}"/>">
            <input type="hidden"
                   name="currentVisitDate"
                   value="<c:out value="${visiter.id.visitDate}"/>">
          </c:otherwise>
        </c:choose>

        <p>
          <label for="codeMed">Mdecin :</label>
          <select id="codeMed" name="codeMed" required>
            <c:forEach items="${medecins}" var="medecin">
              <option value="${medecin.codeMed}"
                <c:if test="${visiter.medecin.codeMed eq medecin.codeMed}">
                  selected
                </c:if>>
                <c:out value="${medecin.codeMed}"/>
                
                <c:out value="${medecin.nom}"/>
                <c:out value="${medecin.prenom}"/>
              </option>
            </c:forEach>
          </select>
        </p>

        <p>
          <label for="codePat">Patient :</label>
          <select id="codePat" name="codePat" required>
            <c:forEach items="${patients}" var="patient">
              <option value="${patient.codePat}"
                <c:if test="${visiter.patient.codePat eq patient.codePat}">
                  selected
                </c:if>>
                <c:out value="${patient.codePat}"/>
                
                <c:out value="${patient.nom}"/>
                <c:out value="${patient.prenom}"/>
              </option>
            </c:forEach>
          </select>
        </p>

        <p>
          <label for="visitDate">Date :</label>
          <input id="visitDate"
                 type="date"
                 name="visitDate"
                 value="<c:out value="${visiter.visitDate}"/>"
                 required>
        </p>

        <button type="submit">Enregistrer</button>
        <a href="${pageContext.request.contextPath}/visites">Annuler</a>
      </form>
    </c:if>
  </main>
</body>
</html>
