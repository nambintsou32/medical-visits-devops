package io.github.nambintsou32.medicalvisits.web;

import java.io.IOException;
import java.util.List;

import io.github.nambintsou32.medicalvisits.domain.Medecin;
import io.github.nambintsou32.medicalvisits.persistence.MedecinRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/medecins/*")
public final class MedecinServlet extends HttpServlet {

    private MedecinRepository medecinRepository;

    @Override
    public void init() {
        EntityManagerFactory entityManagerFactory =
                DatabaseStartupListener.getEntityManagerFactory(
                    getServletContext()
                );

        medecinRepository = new MedecinRepository(entityManagerFactory);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String path = request.getPathInfo();

        if ("/new".equals(path)) {
            showForm(request, response, null, null);
            return;
        }

        if ("/edit".equals(path)) {
            String codeMed = requiredParameter(request, "codeMed");

            Medecin medecin = medecinRepository.findByCode(codeMed)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Mdecin introuvable : " + codeMed
                    ));

            showForm(request, response, medecin, null);
            return;
        }

        showList(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = requiredParameter(request, "action");

        try {
            switch (action) {
                case "create" -> createMedecin(request);
                case "update" -> updateMedecin(request);
                case "delete" -> deleteMedecin(request);
                default -> throw new IllegalArgumentException(
                    "Action inconnue : " + action
                );
            }

            response.sendRedirect(
                request.getContextPath()
                    + "/medecins?message="
                    + action
            );
        } catch (IllegalArgumentException exception) {
            if ("delete".equals(action)) {
                request.setAttribute("error", exception.getMessage());
                showList(request, response);
            } else {
                showForm(request, response, null, exception.getMessage());
            }
        }
    }

    private void showList(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        List<Medecin> medecins = medecinRepository.findAll();

        request.setAttribute("medecins", medecins);
        request.getRequestDispatcher("/WEB-INF/jsp/medecins.jsp")
                .forward(request, response);
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Medecin medecin,
            String error
    ) throws ServletException, IOException {
        request.setAttribute("medecin", medecin);
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/jsp/medecin-form.jsp")
                .forward(request, response);
    }

    private void createMedecin(HttpServletRequest request) {
        medecinRepository.create(new Medecin(
            requiredParameter(request, "codeMed"),
            requiredParameter(request, "nom"),
            requiredParameter(request, "prenom"),
            requiredParameter(request, "grade")
        ));
    }

    private void updateMedecin(HttpServletRequest request) {
        String codeMed = requiredParameter(request, "codeMed");

        Medecin medecin = medecinRepository.findByCode(codeMed)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Mdecin introuvable : " + codeMed
                ));

        medecin.setNom(requiredParameter(request, "nom"));
        medecin.setPrenom(requiredParameter(request, "prenom"));
        medecin.setGrade(requiredParameter(request, "grade"));

        medecinRepository.update(medecin);
    }

    private void deleteMedecin(HttpServletRequest request) {
        String codeMed = requiredParameter(request, "codeMed");

        if (!medecinRepository.deleteByCode(codeMed)) {
            throw new IllegalArgumentException(
                "Mdecin introuvable : " + codeMed
            );
        }
    }

    private static String requiredParameter(
            HttpServletRequest request,
            String name
    ) {
        String value = request.getParameter(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Le champ " + name + " est obligatoire"
            );
        }

        return value.trim();
    }
}
