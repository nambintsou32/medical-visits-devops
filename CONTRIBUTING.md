# Contributing

## Workflow

1. Synchronize the local `main` branch.
2. Create a short-lived branch.
3. Make small and focused changes.
4. Test the changes locally.
5. Commit using Conventional Commits.
6. Push the branch and open a pull request.
7. Merge only after the required checks succeed.

## Branch naming

- `feature/`: application functionality
- `fix/`: bug fixes
- `ci/`: CI/CD pipeline changes
- `deploy/`: deployment configuration
- `docs/`: documentation
- `chore/`: maintenance and repository configuration

Examples:

```text
feature/patient-search
ci/jenkins-pipeline
deploy/k3s-medical-app
fix/mysql-readiness-probe
