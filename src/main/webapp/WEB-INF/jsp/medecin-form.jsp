<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Mdecin  Gestion des visites mdicales</title>
</head>
<body>
  <main>
    <p>
      <a href="${pageContext.request.contextPath}/medecins">
        Retour  la liste des mdecins
      </a>
    </p>

    <c:choose>
      <c:when test="${empty medecin}">
        <h1>Ajouter un mdecin</h1>
      </c:when>
      <c:otherwise>
        <h1>Modifier le mdecin <c:out value="${medecin.codeMed}"/></h1>
      </c:otherwise>
    </c:choose>

    <c:if test="${not empty error}">
      <p><strong>Erreur : <c:out value="${error}"/></strong></p>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/medecins">
      <c:choose>
        <c:when test="${empty medecin}">
          <input type="hidden" name="action" value="create">

          <p>
            <label for="codeMed">Code mdecin :</label>
            <input id="codeMed" name="codeMed" maxlength="30" required>
          </p>
        </c:when>
        <c:otherwise>
          <input type="hidden" name="action" value="update">
          <input type="hidden"
                 name="codeMed"
                 value="<c:out value="${medecin.codeMed}"/>">

          <p>
            Code mdecin :
            <strong><c:out value="${medecin.codeMed}"/></strong>
          </p>
        </c:otherwise>
      </c:choose>

      <p>
        <label for="nom">Nom :</label>
        <input id="nom"
               name="nom"
               maxlength="100"
               value="<c:out value="${medecin.nom}"/>"
               required>
      </p>

      <p>
        <label for="prenom">Prnom :</label>
        <input id="prenom"
               name="prenom"
               maxlength="100"
               value="<c:out value="${medecin.prenom}"/>"
               required>
      </p>

      <p>
        <label for="grade">Grade :</label>
        <input id="grade"
               name="grade"
               maxlength="100"
               value="<c:out value="${medecin.grade}"/>"
               required>
      </p>

      <button type="submit">Enregistrer</button>
      <a href="${pageContext.request.contextPath}/medecins">Annuler</a>
    </form>
  </main>
</body>
</html>
