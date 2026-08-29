package io.github.nambintsou32.medicalvisits.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.nambintsou32.medicalvisits.domain.Patient;
import io.github.nambintsou32.medicalvisits.domain.Sexe;
import io.github.nambintsou32.medicalvisits.persistence.PatientRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/patients/*")
public final class PatientServlet extends HttpServlet {

    private PatientRepository patientRepository;

    @Override
    public void init() {
        EntityManagerFactory factory = DatabaseStartupListener
                .getEntityManagerFactory(getServletContext());
        patientRepository = new PatientRepository(factory);
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
                String code = required(request, "codePat", "code patient");
                Patient patient = patientRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException(
                            "Le patient « " + code + " » est introuvable."
                        ));
                showForm(request, response, patient, true, null);
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
                case "create" -> createPatient(request);
                case "update" -> updatePatient(request);
                case "delete" -> deletePatient(request);
                default -> throw new IllegalArgumentException(
                    "L’action demandée n’est pas valide."
                );
            }
            response.sendRedirect(request.getContextPath() + "/patients?message=" + action);
        } catch (IllegalArgumentException exception) {
            displayPostError(request, response, action, exception.getMessage());
        } catch (PersistenceException exception) {
            getServletContext().log("Échec de l’action patient « " + action + " »", exception);
            String message = switch (String.valueOf(action)) {
                case "create" -> "Ce code patient existe déjà. Choisissez un autre code.";
                case "delete" -> "Ce patient ne peut pas être supprimé car il est lié à une visite.";
                default -> "L’opération sur le patient n’a pas pu être enregistrée.";
            };
            displayPostError(request, response, action, message);
        } catch (RuntimeException exception) {
            getServletContext().log(
                "Erreur inattendue pendant l’action patient « " + action + " »",
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
        String search = trimToNull(request.getParameter("search"));
        List<Patient> patients;
        if (search == null) {
            patients = patientRepository.findAll();
        } else {
            patients = new ArrayList<>(patientRepository.findByNomContaining(search));
            patientRepository.findByCode(search).ifPresent(patient -> {
                boolean alreadyPresent = patients.stream()
                        .anyMatch(item -> item.getCodePat().equals(patient.getCodePat()));
                if (!alreadyPresent) {
                    patients.add(0, patient);
                }
            });
        }
        request.setAttribute("patients", patients);
        request.setAttribute("search", search);
        request.getRequestDispatcher("/WEB-INF/jsp/patients.jsp").forward(request, response);
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Patient patient,
            boolean editing,
            String error
    ) throws ServletException, IOException {
        request.setAttribute("patient", patient);
        request.setAttribute("editing", editing);
        request.setAttribute("formSubmitted", error != null);
        request.setAttribute("sexes", Sexe.values());
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/jsp/patient-form.jsp").forward(request, response);
    }

    private void createPatient(HttpServletRequest request) {
        String code = required(request, "codePat", "code patient");
        if (patientRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Le code patient « " + code + " » existe déjà.");
        }
        patientRepository.create(new Patient(
            code,
            required(request, "nom", "nom"),
            required(request, "prenom", "prénom"),
            requiredSexe(request),
            required(request, "adresse", "adresse")
        ));
    }

    private void updatePatient(HttpServletRequest request) {
        String code = required(request, "codePat", "code patient");
        Patient patient = patientRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Le patient « " + code + " » est introuvable."
                ));
        patient.setNom(required(request, "nom", "nom"));
        patient.setPrenom(required(request, "prenom", "prénom"));
        patient.setSexe(requiredSexe(request));
        patient.setAdresse(required(request, "adresse", "adresse"));
        patientRepository.update(patient);
    }

    private void deletePatient(HttpServletRequest request) {
        String code = required(request, "codePat", "code patient");
        if (!patientRepository.deleteByCode(code)) {
            throw new IllegalArgumentException("Le patient « " + code + " » est introuvable.");
        }
    }

    private static Sexe requiredSexe(HttpServletRequest request) {
        String value = required(request, "sexe", "sexe");
        try {
            return Sexe.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Le sexe sélectionné n’est pas valide.");
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
