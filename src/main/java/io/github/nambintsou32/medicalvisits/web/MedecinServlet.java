package io.github.nambintsou32.medicalvisits.web;

import java.io.IOException;
import java.util.List;

import io.github.nambintsou32.medicalvisits.domain.Medecin;
import io.github.nambintsou32.medicalvisits.persistence.MedecinRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
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
        EntityManagerFactory factory = DatabaseStartupListener
                .getEntityManagerFactory(getServletContext());
        medecinRepository = new MedecinRepository(factory);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String path = request.getPathInfo();
            if ("/new".equals(path)) {
                showForm(request, response, null, false, null);
                return;
            }
            if ("/edit".equals(path)) {
                String code = required(request, "codeMed", "code médecin");
                Medecin medecin = medecinRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException(
                            "Le médecin « " + code + " » est introuvable."
                        ));
                showForm(request, response, medecin, true, null);
                return;
            }
            showList(request, response);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            showList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = trimToNull(request.getParameter("action"));
        try {
            if (action == null) {
                throw new IllegalArgumentException("Aucune action n’a été indiquée.");
            }
            switch (action) {
                case "create" -> createMedecin(request);
                case "update" -> updateMedecin(request);
                case "delete" -> deleteMedecin(request);
                default -> throw new IllegalArgumentException("L’action demandée n’est pas valide.");
            }
            response.sendRedirect(request.getContextPath() + "/medecins?message=" + action);
        } catch (IllegalArgumentException exception) {
            displayPostError(request, response, action, exception.getMessage());
        } catch (PersistenceException exception) {
            getServletContext().log("Échec de l’action médecin « " + action + " »", exception);
            String message = switch (String.valueOf(action)) {
                case "create" -> "Ce code médecin existe déjà. Choisissez un autre code.";
                case "delete" -> "Ce médecin ne peut pas être supprimé car il est lié à une visite.";
                default -> "L’opération sur le médecin n’a pas pu être enregistrée.";
            };
            displayPostError(request, response, action, message);
        } catch (RuntimeException exception) {
            getServletContext().log(
                "Erreur inattendue pendant l’action médecin « " + action + " »",
                exception
            );
            displayPostError(request, response, action,
                "Une erreur technique empêche l’opération. Veuillez réessayer.");
        }
    }

    private void displayPostError(
            HttpServletRequest request,
            HttpServletResponse response,
            String action,
            String message
    ) throws ServletException, IOException {
        request.setAttribute("error", message);
        if ("create".equals(action) || "update".equals(action)) {
            showForm(request, response, null, "update".equals(action), message);
        } else {
            showList(request, response);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Medecin> medecins = medecinRepository.findAll();
        request.setAttribute("medecins", medecins);
        request.getRequestDispatcher("/WEB-INF/jsp/medecins.jsp").forward(request, response);
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Medecin medecin,
            boolean editing,
            String error
    ) throws ServletException, IOException {
        request.setAttribute("medecin", medecin);
        request.setAttribute("editing", editing);
        request.setAttribute("formSubmitted", error != null);
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/jsp/medecin-form.jsp").forward(request, response);
    }

    private void createMedecin(HttpServletRequest request) {
        String code = required(request, "codeMed", "code médecin");
        if (medecinRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Le code médecin « " + code + " » existe déjà.");
        }
        medecinRepository.create(new Medecin(
            code,
            required(request, "nom", "nom"),
            required(request, "prenom", "prénom"),
            required(request, "grade", "grade")
        ));
    }

    private void updateMedecin(HttpServletRequest request) {
        String code = required(request, "codeMed", "code médecin");
        Medecin medecin = medecinRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Le médecin « " + code + " » est introuvable."
                ));
        medecin.setNom(required(request, "nom", "nom"));
        medecin.setPrenom(required(request, "prenom", "prénom"));
        medecin.setGrade(required(request, "grade", "grade"));
        medecinRepository.update(medecin);
    }

    private void deleteMedecin(HttpServletRequest request) {
        String code = required(request, "codeMed", "code médecin");
        if (!medecinRepository.deleteByCode(code)) {
            throw new IllegalArgumentException("Le médecin « " + code + " » est introuvable.");
        }
    }

    private static String required(HttpServletRequest request, String name, String label) {
        String value = trimToNull(request.getParameter(name));
        if (value == null) {
            throw new IllegalArgumentException("Le champ « " + label + " » est obligatoire.");
        }
        return value;
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
