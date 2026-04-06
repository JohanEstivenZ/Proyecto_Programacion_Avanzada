package co.edu.uniquindio.taskflow.domain.enums;

/**
 * Roles de usuario en el sistema TaskFlow.
 *
 * <p>Spring Security usa estos roles para controlar el acceso a los endpoints.
 * En {@code SecurityConfig}, las reglas se definen como
 * {@code hasRole("TEAM_LEAD")}, que internamente busca la authority
 * {@code ROLE_TEAM_LEAD} en el contexto de seguridad.</p>
 *
 * <p>Convencion de Spring Security: el metodo {@code hasRole("X")} agrega
 * automaticamente el prefijo "ROLE_". Por eso en el JWT guardamos "TEAM_LEAD"
 * pero al crear la authority escribimos "ROLE_TEAM_LEAD".</p>
 */
public enum Rol {
    /** Desarrollador: crea tareas y cambia su estado. */
    DEVELOPER,
    /** Lider de equipo: asigna, aprueba y archiva tareas. */
    TEAM_LEAD,
    /** Administrador: acceso total al sistema. */
    ADMIN
}
