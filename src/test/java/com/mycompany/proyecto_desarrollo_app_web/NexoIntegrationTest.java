package com.mycompany.proyecto_desarrollo_app_web;

import com.mycompany.proyecto_desarrollo_app_web.domain.*;
import com.mycompany.proyecto_desarrollo_app_web.repository.*;
import com.mycompany.proyecto_desarrollo_app_web.service.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NexoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired FuentePagoService fuentes;
    @Autowired ImportacionService importaciones;
    @Autowired ConciliacionService conciliaciones;
    @Autowired UsuarioService usuarioService;
    @Autowired FuentePagoRepository fuenteRepo;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired LoteImportacionRepository lotes;
    @Autowired OperacionInternaRepository operaciones;
    @Autowired LiquidacionRepository liquidaciones;
    @Autowired MovimientoProveedorRepository movimientos;
    @Autowired ConciliacionRepository concRepo;
    @Autowired DiferenciaRepository diferencias;
    @Autowired VinculoConciliacionRepository vinculos;
    @Autowired ResolucionDiferenciaRepository resoluciones;
    @Autowired PasswordEncoder encoder;
    private String hash;

    @BeforeAll void clavePrueba() { hash = encoder.encode("ClaveSegura123!"); }
    @BeforeEach void preparar() {
        resoluciones.deleteAll(); diferencias.deleteAll(); vinculos.deleteAll(); concRepo.deleteAll();
        movimientos.deleteAll(); liquidaciones.deleteAll(); operaciones.deleteAll(); lotes.deleteAll(); fuenteRepo.deleteAll();
        usuarios.deleteAll();
        cuenta("bootstrap", "ADMIN");
        cuenta("admin", "ADMIN"); cuenta("analista", "ANALISTA"); cuenta("supervisor", "SUPERVISOR");
        cuenta("mixto", "ANALISTA", "SUPERVISOR");
        SecurityContextHolder.clearContext();
    }
    @AfterEach void limpiarSesion() { SecurityContextHolder.clearContext(); }

    @Test void accesoLoginLogoutYDesactivacion() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Correo electrónico")));
        mvc.perform(post("/login").with(csrf()).param("correo", "analista@nexo.test").param("password", "mal"))
                .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
        var login = mvc.perform(post("/login").with(csrf()).param("correo", "analista@nexo.test").param("password", "ClaveSegura123!"))
                .andExpect(authenticated().withUsername("analista@nexo.test")).andReturn();
        MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/").session(sesion)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("FACTOR_PASSWORD"))));
        mvc.perform(get("/liquidacion/listado").session(sesion)).andExpect(status().isOk());
        mvc.perform(post("/logout").session(sesion).with(csrf())).andExpect(redirectedUrl("/login?logout")).andExpect(unauthenticated());
        var u = usuarios.findByCorreoIgnoreCase("analista@nexo.test").orElseThrow();
        u.setActivo(false); usuarios.save(u);
        mvc.perform(post("/login").with(csrf()).param("correo", u.getCorreo()).param("password", "ClaveSegura123!"))
                .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    @Test void losPermisosYCsrfSeAplicanEnElServidor() throws Exception {
        mvc.perform(get("/usuario/listado").with(user("analista@nexo.test").roles("ANALISTA"))).andExpect(status().isForbidden());
        mvc.perform(get("/conciliacion/listado").with(user("admin@nexo.test").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/fuente-pago/guardar").with(user("admin@nexo.test").roles("ADMIN"))
                .param("nombre","Proveedor").param("codigo","SEG").param("moneda","CRC")).andExpect(status().isForbidden());
        como("analista", "ANALISTA");
        FuentePago f = new FuentePago(); f.setNombre("No autorizado"); f.setCodigo("NO");
        assertThatThrownBy(() -> fuentes.save(f)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test void lasSesionesSeRevocanAlCambiarRoles() throws Exception {
        var login = mvc.perform(post("/login").with(csrf()).param("correo", "analista@nexo.test").param("password", "ClaveSegura123!"))
                .andExpect(authenticated()).andReturn();
        MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);
        var u = usuarios.findByCorreoIgnoreCase("analista@nexo.test").orElseThrow();
        u.setRoles(Set.of(roles.findByCodigo("SUPERVISOR").orElseThrow())); usuarios.save(u);
        mvc.perform(get("/liquidacion/listado").session(sesion)).andExpect(redirectedUrl("/login?sesion"));
    }

    @Test void registroDeUsuariosValidaCorreoRolesYHash() throws Exception {
        var analista = roles.findByCodigo("ANALISTA").orElseThrow();
        var supervisor = roles.findByCodigo("SUPERVISOR").orElseThrow();
        mvc.perform(post("/usuario/guardar").with(user("admin@nexo.test").roles("ADMIN")).with(csrf())
                .param("nombre","Nuevo").param("correo","nuevo@nexo.test").param("password","ClaveSegura123!")
                .param("roles",analista.getId().toString(),supervisor.getId().toString()).param("activo","true"))
                .andExpect(redirectedUrl("/usuario/listado"));
        var nuevo = usuarios.findByCorreoIgnoreCase("nuevo@nexo.test").orElseThrow();
        assertThat(nuevo.getRoles()).hasSize(2); assertThat(nuevo.getPasswordHash()).isNotEqualTo("ClaveSegura123!");
        assertThat(encoder.matches("ClaveSegura123!", nuevo.getPasswordHash())).isTrue();
        mvc.perform(post("/usuario/guardar").with(user("admin@nexo.test").roles("ADMIN")).with(csrf())
                .param("nombre","Duplicado").param("correo","NUEVO@nexo.test").param("password","ClaveSegura123!")
                .param("roles",analista.getId().toString())).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("El correo ya está registrado")));
    }

    @Test void crudDeFuenteValidaDuplicadosYConservaDatos() throws Exception {
        FuentePago f = fuente("P1");
        como("admin", "ADMIN");
        FuentePago repetida = new FuentePago(); repetida.setNombre("Otra"); repetida.setCodigo(" p1 ");
        assertThatThrownBy(() -> fuentes.save(repetida)).isInstanceOf(IllegalArgumentException.class);
        f.setNombre("Actualizado"); fuentes.save(f); fuentes.cambiarEstado(f.getIdFuentePago(), false);
        assertThat(fuenteRepo.findById(f.getIdFuentePago()).orElseThrow().getNombre()).isEqualTo("Actualizado");
        assertThat(fuentes.getFuentesPago(true)).isEmpty();
        fuentes.cambiarEstado(f.getIdFuentePago(), true); fuentes.delete(f.getIdFuentePago());
        assertThat(fuenteRepo.count()).isZero();
        mvc.perform(post("/fuente-pago/guardar").with(user("admin@nexo.test").roles("ADMIN")).with(csrf())
                .param("nombre", "").param("codigo", "CONSERVAR").param("moneda", "XXX"))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("El nombre es obligatorio")));
    }

    @Test void importacionEsAtomicaYRegistraErroresYDuplicados() throws Exception {
        FuentePago f = fuente("CSV"); como("analista", "ANALISTA");
        var error = importar(f, "INTERNO", "invalido");
        assertThat(error.getEstado()).isEqualTo("ERROR"); assertThat(error.getCantidad()).isZero();
        assertThat(operaciones.count()).isZero(); assertThat(error.getResultado()).contains("Registro 2");
        var valida = importar(f, "INTERNO", "interno"); assertThat(valida.getEstado()).isEqualTo("VALIDO");
        assertThat(valida.getCantidad()).isEqualTo(5); assertThat(valida.getUsuario().getCorreo()).isEqualTo("analista@nexo.test");
        assertThat(importar(f,"INTERNO","interno").getEstado()).isEqualTo("ERROR");
        assertThat(operaciones.count()).isEqualTo(5); assertThat(lotes.count()).isEqualTo(3);
    }

    @Test void fuenteInactivaYMonedaIncompatibleImpidenImportar() throws Exception {
        FuentePago f = fuente("INACTIVA"); como("admin","ADMIN"); fuentes.cambiarEstado(f.getIdFuentePago(),false);
        como("analista","ANALISTA"); assertThat(importar(f,"INTERNO","interno").getResultado()).contains("inactiva");
        como("admin","ADMIN"); fuentes.cambiarEstado(f.getIdFuentePago(),true);
        como("analista","ANALISTA");
        String csv="liquidacion,referencia,tipo,fecha,importe,moneda,referencia_original\nL,A,COBRO,2026-11-01,10.00,USD,\n";
        assertThat(importaciones.importar(f.getIdFuentePago(),"INTERNO",archivo(csv)).getResultado()).contains("moneda");
        assertThat(operaciones.count()).isZero();
    }

    @Test void flujoCompletoConDiferenciasResolucionesYCierre() throws Exception {
        FuentePago f = fuente("FLUJO"); como("analista","ANALISTA");
        assertThat(importar(f,"INTERNO","interno").getEstado()).isEqualTo("VALIDO");
        assertThat(importar(f,"PROVEEDOR","proveedor").getEstado()).isEqualTo("VALIDO");
        var l=liquidaciones.findByFuentePago_IdFuentePagoAndReferencia(f.getIdFuentePago(),"LIQ-001").orElseThrow();
        var c=conciliaciones.iniciar(l.getId());
        for(var m:movimientos.findByLiquidacion_IdOrderByIdAsc(l.getId()))
            if(Set.of("OP-001","OP-002","DEV-001").contains(m.getReferencia())) conciliaciones.vincular(c.getId(),m.getId(),m.getReferencia());
        var pendientes=conciliaciones.diferencias(c.getId()); assertThat(pendientes).hasSize(4);
        assertThat(pendientes).extracting(Diferencia::getTipo).contains("IMPORTE","SIN_OPERACION","SIN_MOVIMIENTO");
        assertThat(conciliaciones.obtener(c.getId()).getTotalCalculado()).isEqualByComparingTo("1580.00");
        assertThat(conciliaciones.obtener(c.getId()).getTotalInterno()).isEqualByComparingTo("1470.00");
        como("supervisor","SUPERVISOR"); assertThatThrownBy(() -> conciliaciones.cerrar(c.getId())).hasMessageContaining("pendientes");
        for(var d:pendientes) {
            como("analista","ANALISTA"); conciliaciones.proponer(d.getId(),"OTRO","Diferencia investigada y documentada para la demostración.");
            como("supervisor","SUPERVISOR"); conciliaciones.evaluar(conciliaciones.historial(d.getId()).getFirst().getId(),true,"");
        }
        conciliaciones.cerrar(c.getId()); var cerrada=conciliaciones.obtener(c.getId());
        assertThat(cerrada.getEstado()).isEqualTo("CERRADA"); assertThat(cerrada.getAutorizante().getCorreo()).isEqualTo("supervisor@nexo.test");
        assertThat(cerrada.getFechaCierre()).isNotNull();
        assertThat(operaciones.findByFuentePago_IdFuentePagoAndReferenciaLiquidacionOrderByIdAsc(f.getIdFuentePago(),"LIQ-001")
                .stream().filter(o -> o.getReferencia().equals("OP-002")).findFirst().orElseThrow().getImporte()).isEqualByComparingTo("500.00");
        como("analista","ANALISTA");
        assertThatThrownBy(() -> conciliaciones.proponer(pendientes.getFirst().getId(),"OTRO","Nuevo intento")).hasMessageContaining("cerrada");
        assertThat(importaciones.importar(f.getIdFuentePago(),"INTERNO",archivo("liquidacion,referencia,tipo,fecha,importe,moneda,referencia_original\nLIQ-001,NUEVA,COBRO,2026-11-01,1.00,CRC,\n")).getEstado()).isEqualTo("ERROR");
        comprobarVistas(c.getId(),l.getId(),pendientes.getFirst().getId());
    }

    @Test void coincidenciaExactaNoPermiteVinculosRepetidos() throws Exception {
        FuentePago f=fuente("EXACTA"); como("analista","ANALISTA"); importar(f,"INTERNO","interno"); importar(f,"PROVEEDOR","proveedor");
        var l=liquidaciones.findByFuentePago_IdFuentePagoAndReferencia(f.getIdFuentePago(),"LIQ-002").orElseThrow();
        var c=conciliaciones.iniciar(l.getId()); var m=movimientos.findByLiquidacion_IdOrderByIdAsc(l.getId()).getFirst();
        conciliaciones.vincular(c.getId(),m.getId(),"op-ok"); assertThat(conciliaciones.diferencias(c.getId())).isEmpty();
        assertThatThrownBy(() -> conciliaciones.vincular(c.getId(),m.getId(),"OP-OK")).hasMessageContaining("vinculado");
        assertThatThrownBy(() -> conciliaciones.iniciar(l.getId())).hasMessageContaining("ya tiene");
        como("supervisor","SUPERVISOR"); conciliaciones.cerrar(c.getId());
        assertThat(conciliaciones.listar("liq-002","CERRADA")).hasSize(1);
        assertThat(conciliaciones.listar("inexistente","")).isEmpty();
    }

    @Test void noHayAutoaprobacionYElRechazoConservaElHistorial() throws Exception {
        FuentePago f=fuente("HISTORIA"); como("analista","ANALISTA"); importar(f,"PROVEEDOR","proveedor");
        var l=liquidaciones.findByFuentePago_IdFuentePagoAndReferencia(f.getIdFuentePago(),"LIQ-002").orElseThrow();
        var c=conciliaciones.iniciar(l.getId()); var d=conciliaciones.diferencias(c.getId()).getFirst();
        como("mixto","ANALISTA","SUPERVISOR"); conciliaciones.proponer(d.getId(),"OTRO","Primera explicación.");
        var r=conciliaciones.historial(d.getId()).getFirst();
        assertThatThrownBy(() -> conciliaciones.evaluar(r.getId(),true,"")).hasMessageContaining("propia propuesta");
        como("supervisor","SUPERVISOR");
        assertThatThrownBy(() -> conciliaciones.evaluar(r.getId(),false,"")).hasMessageContaining("motivo");
        conciliaciones.evaluar(r.getId(),false,"Falta comprobar el respaldo del proveedor.");
        como("analista","ANALISTA"); conciliaciones.proponer(d.getId(),"OTRO","Segunda explicación con respaldo corregido.");
        como("supervisor","SUPERVISOR"); var historia=conciliaciones.historial(d.getId());
        conciliaciones.evaluar(historia.getLast().getId(),true,"");
        assertThat(conciliaciones.historial(d.getId())).extracting(ResolucionDiferencia::getEstado).containsExactly("RECHAZADA","APROBADA");
        assertThat(conciliaciones.historial(d.getId()).getFirst().getMotivoRechazo()).isNotBlank();
    }

    @Test void elTotalReportadoInconsistenteGeneraUnaDiferencia() {
        FuentePago f=fuente("TOTAL"); como("analista","ANALISTA");
        String csv="liquidacion,fecha_liquidacion,total_reportado,referencia,tipo,fecha,importe,moneda,referencia_original\nL-TOTAL,2026-11-01,9.00,A,COBRO,2026-11-01,10.00,CRC,\n";
        assertThat(importaciones.importar(f.getIdFuentePago(),"PROVEEDOR",archivo(csv)).getEstado()).isEqualTo("VALIDO");
        var l=liquidaciones.findByFuentePago_IdFuentePagoAndReferencia(f.getIdFuentePago(),"L-TOTAL").orElseThrow();
        var c=conciliaciones.iniciar(l.getId());
        assertThat(conciliaciones.diferencias(c.getId())).extracting(Diferencia::getTipo).contains("TOTAL_LIQUIDACION");
        assertThat(conciliaciones.obtener(c.getId()).getDiferenciaTotal()).isEqualByComparingTo("1.00");
    }

    @Test void laVinculacionNoCruzaFuentesNiLiquidaciones() throws Exception {
        FuentePago a=fuente("ORIGEN_A"), b=fuente("ORIGEN_B");
        como("analista","ANALISTA");
        importar(a,"PROVEEDOR","proveedor"); importar(b,"INTERNO","interno");
        var l=liquidaciones.findByFuentePago_IdFuentePagoAndReferencia(a.getIdFuentePago(),"LIQ-002").orElseThrow();
        var c=conciliaciones.iniciar(l.getId()); var m=movimientos.findByLiquidacion_IdOrderByIdAsc(l.getId()).getFirst();
        assertThatThrownBy(() -> conciliaciones.vincular(c.getId(),m.getId(),"OP-OK")).hasMessageContaining("esta liquidación");
        var otra=liquidaciones.findByFuentePago_IdFuentePagoAndReferencia(a.getIdFuentePago(),"LIQ-001").orElseThrow();
        var otroMovimiento=movimientos.findByLiquidacion_IdOrderByIdAsc(otra.getId()).getFirst();
        assertThatThrownBy(() -> conciliaciones.vincular(c.getId(),otroMovimiento.getId(),"OP-001")).hasMessageContaining("otra liquidación");
        assertThat(vinculos.count()).isZero();
    }

    @Test void noPermiteDesactivarAlUltimoAdministrador() {
        var inicial=usuarios.findByCorreoIgnoreCase("bootstrap@nexo.test").orElseThrow();
        inicial.setActivo(false); usuarios.save(inicial);
        como("admin","ADMIN");
        var admin=usuarios.findByCorreoIgnoreCase("admin@nexo.test").orElseThrow();
        var formulario=usuarioService.formulario(admin.getId()); formulario.setActivo(false);
        assertThatThrownBy(() -> usuarioService.guardar(formulario)).hasMessageContaining("administrador activo");
        assertThat(usuarios.findById(admin.getId()).orElseThrow().isActivo()).isTrue();
    }

    @Test void parserCsvRechazaComillasIncorrectasYUtf8Invalido() {
        assertThat(ArchivoCsv.leer("a,b\n\"x,y\",\"a\"\"b\"\n".getBytes(StandardCharsets.UTF_8)).get(1)).containsExactly("x,y","a\"b");
        assertThatThrownBy(() -> ArchivoCsv.leer("a,b\n\"sin cerrar".getBytes(StandardCharsets.UTF_8))).hasMessageContaining("comillas");
        assertThatThrownBy(() -> ArchivoCsv.leer(new byte[]{(byte)0xC3, (byte)0x28})).hasMessageContaining("UTF-8");
    }

    private void comprobarVistas(Long c,Long l,Long d) throws Exception {
        for(String ruta:List.of("/","/liquidacion/listado","/liquidacion/detalle/"+l,"/conciliacion/listado","/conciliacion/detalle/"+c,
                "/diferencia/listado","/diferencia/detalle/"+d,"/reporte/conciliaciones","/reporte/diferencias","/importacion/listado"))
            mvc.perform(get(ruta).with(user("analista@nexo.test").roles("ANALISTA"))).andExpect(status().isOk());
        for(String ruta:List.of("/","/usuario/listado","/fuente-pago/listado","/importacion/listado"))
            mvc.perform(get(ruta).with(user("admin@nexo.test").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/diferencia/detalle/"+d).with(user("supervisor@nexo.test").roles("SUPERVISOR"))).andExpect(status().isOk());
        mvc.perform(get("/webjars/bootstrap/css/bootstrap.min.css")).andExpect(status().isOk());
    }
    private FuentePago fuente(String codigo) {
        como("admin","ADMIN"); FuentePago f=new FuentePago(); f.setNombre("Proveedor de prueba"); f.setCodigo(codigo); f.setMoneda("CRC"); fuentes.save(f);
        return fuenteRepo.findByCodigo(codigo).orElseThrow();
    }
    private void cuenta(String nombre,String... permisos) {
        Usuario u=usuarios.findByCorreoIgnoreCase(nombre+"@nexo.test").orElse(new Usuario());
        u.setNombre(nombre); u.setCorreo(nombre+"@nexo.test"); u.setActivo(true); u.setPasswordHash(hash);
        Set<Rol> rs=new HashSet<>(); for(String p:permisos) rs.add(roles.findByCodigo(p).orElseThrow()); u.setRoles(rs); usuarios.save(u);
    }
    private void como(String nombre,String... permisos) {
        String[] autoridades=Arrays.stream(permisos).map(p -> "ROLE_"+p).toArray(String[]::new);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(nombre+"@nexo.test","",AuthorityUtils.createAuthorityList(autoridades)));
    }
    private LoteImportacion importar(FuentePago f,String origen,String ejemplo) throws Exception {
        byte[] bytes=new ClassPathResource("ejemplos/"+ejemplo+".csv").getContentAsByteArray();
        return importaciones.importar(f.getIdFuentePago(),origen,new MockMultipartFile("archivo",ejemplo+".csv","text/csv",bytes));
    }
    private MockMultipartFile archivo(String texto) { return new MockMultipartFile("archivo","prueba.csv","text/csv",texto.getBytes(StandardCharsets.UTF_8)); }
}
