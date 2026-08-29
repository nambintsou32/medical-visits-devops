package io.github.nambintsou32.medicalvisits.web;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import io.github.nambintsou32.medicalvisits.domain.Visiter;
import io.github.nambintsou32.medicalvisits.domain.VisiterId;
import io.github.nambintsou32.medicalvisits.persistence.MedecinRepository;
import io.github.nambintsou32.medicalvisits.persistence.PatientRepository;
import io.github.nambintsou32.medicalvisits.persistence.VisiterRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/visites/*")
public final class VisiterServlet extends HttpServlet {

    private MedecinRepository medecinRepository;
    private PatientRepository patientRepository;
    private VisiterRepository visiterRepository;

    @Override
    public void init() {
        EntityManagerFactory factory = DatabaseStartupListener
                .getEntityManagerFactory(getServletContext());
        medecinRepository = new MedecinRepository(factory);
        patientRepository = new PatientRepository(factory);
        visiterRepository = new VisiterRepository(factory);
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
                VisiterId id = visitIdFromRequest(
                    request, "codeMed", "codePat", "visitDate"
                );
                Visiter visit = visiterRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                            "La visite demandée est introuvable."
                        ));
                showForm(request, response, visit, true, null);
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
                case "create" -> createVisit(request);
                case "replace" -> replaceVisit(request);
                case "delete" -> deleteVisit(request);
                default -> throw new IllegalArgumentException("L’action demandée n’est pas valide.");
            }
            response.sendRedirect(request.getContextPath() + "/visites?message=" + action);
        } catch (IllegalArgumentException exception) {
            displayPostError(request, response, action, exception.getMessage());
        } catch (PersistenceException exception) {
            getServletContext().log("Échec de l’action visite « " + action + " »", exception);
            displayPostError(
                request,
                response,
                action,
                "La visite n’a pas pu être enregistrée. Vérifiez qu’elle n’existe pas déjà."
            );
        } catch (RuntimeException exception) {
            getServletContext().log(
                "Erreur inattendue pendant l’action visite « " + action + " »",
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
        if ("create".equals(action) || "replace".equals(action)) {
            showForm(request, response, null, "replace".equals(action), message);
        } else {
            showList(request, response);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Visiter> visits = visiterRepository.findAll();
        request.setAttribute("visites", visits);
        request.getRequestDispatcher("/WEB-INF/jsp/visites.jsp").forward(request, response);
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Visiter visit,
            boolean editing,
            String error
    ) throws ServletException, IOException {
        request.setAttribute("visiter", visit);
        request.setAttribute("editing", editing);
        request.setAttribute("formSubmitted", error != null);
        request.setAttribute("medecins", medecinRepository.findAll());
        request.setAttribute("patients", patientRepository.findAll());
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/jsp/visite-form.jsp").forward(request, response);
    }

    private void createVisit(HttpServletRequest request) {
        String codeMed = required(request, "codeMed", "médecin");
        String codePat = required(request, "codePat", "patient");
        LocalDate date = requiredDate(request, "visitDate", "date de visite");
        validateReferences(codeMed, codePat);
        VisiterId id = new VisiterId(codeMed, codePat, date);
        if (visiterRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException(
                "Une visite identique existe déjà pour ce médecin, ce patient et cette date."
            );
        }
        visiterRepository.create(codeMed, codePat, date);
    }

    private void replaceVisit(HttpServletRequest request) {
        VisiterId currentId = visitIdFromRequest(
            request, "currentCodeMed", "currentCodePat", "currentVisitDate"
        );
        if (visiterRepository.findById(currentId).isEmpty()) {
            throw new IllegalArgumentException("La visite à modifier est introuvable.");
        }

        String codeMed = required(request, "codeMed", "médecin");
        String codePat = required(request, "codePat", "patient");
        LocalDate date = requiredDate(request, "visitDate", "date de visite");
        validateReferences(codeMed, codePat);
        VisiterId replacementId = new VisiterId(codeMed, codePat, date);
        if (!currentId.equals(replacementId)
                && visiterRepository.findById(replacementId).isPresent()) {
            throw new IllegalArgumentException(
                "Une visite identique existe déjà pour ce médecin, ce patient et cette date."
            );
        }
        visiterRepository.replace(currentId, codeMed, codePat, date);
    }

    private void validateReferences(String codeMed, String codePat) {
        if (medecinRepository.findByCode(codeMed).isEmpty()) {
            throw new IllegalArgumentException("Le médecin sélectionné n’existe plus.");
        }
        if (patientRepository.findByCode(codePat).isEmpty()) {
            throw new IllegalArgumentException("Le patient sélectionné n’existe plus.");
        }
    }

    private void deleteVisit(HttpServletRequest request) {
        VisiterId id = visitIdFromRequest(request, "codeMed", "codePat", "visitDate");
        if (!visiterRepository.deleteById(id)) {
            throw new IllegalArgumentException("La visite à supprimer est introuvable.");
        }
    }

    private static VisiterId visitIdFromRequest(
            HttpServletRequest request,
            String codeMedParameter,
            String codePatParameter,
            String dateParameter
    ) {
        return new VisiterId(
            required(request, codeMedParameter, "médecin"),
            required(request, codePatParameter, "patient"),
            requiredDate(request, dateParameter, "date de visite")
        );
    }

    private static LocalDate requiredDate(
            HttpServletRequest request,
            String name,
            String label
    ) {
        String value = required(request, name, label);
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La date de visite n’est pas valide.");
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
