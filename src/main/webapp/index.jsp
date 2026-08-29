<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Gestion des visites mdicales</title>
</head>
<body>
  <main>
    <h1>Gestion des visites dans un centre mdical</h1>

    <p>Application JSP avec Hibernate, MySQL et Flyway.</p>

    <nav>
      <ul>
        <li><a href="${pageContext.request.contextPath}/patients">Patients</a></li>
        <li><a href="${pageContext.request.contextPath}/medecins">Mdecins</a></li>
        <li><a href="${pageContext.request.contextPath}/visites">Visites</a></li>
        <li><a href="${pageContext.request.contextPath}/health">tat du service</a></li>
      </ul>
    </nav>
  </main>
</body>
</html>
