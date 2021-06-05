package ar.edu.unlam.tallerweb1.controladores;

import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.unlam.tallerweb1.modelo.Auto;
import ar.edu.unlam.tallerweb1.modelo.Garage;
import ar.edu.unlam.tallerweb1.servicios.ServicioAuto;
import ar.edu.unlam.tallerweb1.servicios.ServicioCliente;
import ar.edu.unlam.tallerweb1.servicios.ServicioEstacionamiento;
import ar.edu.unlam.tallerweb1.servicios.ServicioGarage;
import ar.edu.unlam.tallerweb1.servicios.ServicioRegistro;

@Controller
public class ControladorEstacionamiento {
	private ServicioAuto servicioAuto;
	private ServicioGarage servicioGarage;
	private ServicioEstacionamiento servicioEst;
	
	@Autowired
	public ControladorEstacionamiento(ServicioGarage servicioGarage,ServicioAuto servicioAuto,ServicioEstacionamiento servicioEst){
		this.servicioGarage= servicioGarage;
		this.servicioAuto = servicioAuto;
		this.servicioEst = servicioEst;
	}
	@RequestMapping(path="/sacarAutoDeGarage/{GarageId}/{AutoId}", method=RequestMethod.GET)
	public ModelAndView SacarAutosDeGarage( @PathVariable("GarageId")Long Gid,
			@PathVariable("AutoId")Long Aid,
			HttpServletRequest request
			){
		String rol = (String) request.getSession().getAttribute("roll");
		if(rol != null)
			if(rol.equals("admin")) {
				
			
		ModelMap modelo = new ModelMap();
			Garage garage2 = servicioGarage.buscarGarage(Gid);
			HashSet<Auto> autos = (HashSet<Auto>) servicioEst.buscarAutosQueEstenActivosEnUnGarage(garage2);
			Auto autoSalir = servicioAuto.buscarAuto(Aid);
			//servicioAuto.cambiarEstadoDeSiestaEnGarageOno(auto);	
			
			for(Auto e: autos) {
				if(e.getId().equals(autoSalir.getId())) {
					servicioAuto.cambiarEstadoDeSiestaEnGarageOno(e);
					servicioGarage.restarContador(garage2);
				}
			}
			modelo.put("auto", autos);
			return new ModelAndView("redirect:/mostrarAutosDeUnGarage/{GarageId}", modelo);
			}
		return new ModelAndView("redirect:/login");
	}
	

	
}