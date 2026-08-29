<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Patient  Gestion des visites mdicales</title>
</head>
<body>
  <main>
    <p>
      <a href="${pageContext.request.contextPath}/patients">
        Retour  la liste des patients
      </a>
    </p>

    <c:choose>
      <c:when test="${empty patient}">
        <h1>Ajouter un patient</h1>
      </c:when>
      <c:otherwise>
        <h1>Modifier le patient <c:out value="${patient.codePat}"/></h1>
      </c:otherwise>
    </c:choose>

    <c:if test="${not empty error}">
      <p><strong>Erreur : <c:out value="${error}"/></strong></p>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/patients">
      <c:choose>
        <c:when test="${empty patient}">
          <input type="hidden" name="action" value="create">

          <p>
            <label for="codePat">Code patient :</label>
            <input id="codePat" name="codePat" maxlength="30" required>
          </p>
        </c:when>
        <c:otherwise>
          <input type="hidden" name="action" value="update">
          <input type="hidden"
                 name="codePat"
                 value="<c:out value="${patient.codePat}"/>">

          <p>
            Code patient :
            <strong><c:out value="${patient.codePat}"/></strong>
          </p>
        </c:otherwise>
      </c:choose>

      <p>
        <label for="nom">Nom :</label>
        <input id="nom"
               name="nom"
               maxlength="100"
               value="<c:out value="${patient.nom}"/>"
               required>
      </p>

      <p>
        <label for="prenom">Prnom :</label>
        <input id="prenom"
               name="prenom"
               maxlength="100"
               value="<c:out value="${patient.prenom}"/>"
               required>
      </p>

      <p>
        <label for="sexe">Sexe :</label>
        <select id="sexe" name="sexe" required>
          <c:forEach items="${sexes}" var="sexe">
            <option value="${sexe}"
              <c:if test="${patient.sexe eq sexe}">selected</c:if>>
              <c:out value="${sexe}"/>
            </option>
          </c:forEach>
        </select>
      </p>

      <p>
        <label for="adresse">Adresse :</label>
        <input id="adresse"
               name="adresse"
               maxlength="255"
               value="<c:out value="${patient.adresse}"/>"
               required>
      </p>

      <button type="submit">Enregistrer</button>
      <a href="${pageContext.request.contextPath}/patients">Annuler</a>
    </form>
  </main>
</body>
</html>
