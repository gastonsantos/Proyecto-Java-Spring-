
package ar.edu.unlam.tallerweb1.controladores;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.unlam.tallerweb1.modelo.Garage;
import ar.edu.unlam.tallerweb1.modelo.Auto;
import ar.edu.unlam.tallerweb1.modelo.Billetera;
import ar.edu.unlam.tallerweb1.modelo.Cliente;
import ar.edu.unlam.tallerweb1.modelo.Estacionamiento;
import ar.edu.unlam.tallerweb1.servicios.ServicioAuto;
import ar.edu.unlam.tallerweb1.servicios.ServicioBilletera;
import ar.edu.unlam.tallerweb1.servicios.ServicioCliente;
import ar.edu.unlam.tallerweb1.servicios.ServicioCobrarTickets;
import ar.edu.unlam.tallerweb1.servicios.ServicioEstacionamiento;
import ar.edu.unlam.tallerweb1.servicios.ServicioGarage;

@Controller
public class ControladorPagarGarage {
	private ServicioEstacionamiento servicioEst;
	private ServicioCobrarTickets servicioCobrarTickets;
	private ServicioAuto servicioAuto;
	private ServicioCliente servicioCliente;
	private ServicioGarage servicioGarage;
	private ServicioBilletera servicioBilletera;
	
	@Autowired
	public ControladorPagarGarage(ServicioCobrarTickets servicioCobrarTickets,ServicioAuto servicioAuto,ServicioCliente servicioCliente,ServicioEstacionamiento servicioEst,ServicioGarage servicioGarage, ServicioBilletera servicioBilletera) {
		this.servicioCobrarTickets = servicioCobrarTickets;
		this.servicioAuto = servicioAuto;
		this.servicioCliente = servicioCliente;
		this.servicioEst = servicioEst;
		this.servicioGarage= servicioGarage;
		this.servicioBilletera = servicioBilletera;
	}
	
	@RequestMapping(path="/mostrarFormularioReservaEstadia/{cliente.id}/{auto.id}/{garage.id}", method=RequestMethod.GET)
	public ModelAndView mostrarFormularioReservaEstadia(@PathVariable("cliente.id") Long idCliente,
														@PathVariable("auto.id") Long idAuto,
														@PathVariable("garage.id") Long idGarage,
														HttpServletRequest request) {
		
		String rol = (String) request.getSession().getAttribute("roll");
		if(rol != null) {
			if(rol.equals("cliente")) {
				ModelMap modelo = new ModelMap();
				Estacionamiento ticket = new Estacionamiento();
				Auto auto1 = servicioAuto.buscarAuto(idAuto);
				Cliente cliente1 = servicioCliente.consultarClientePorId(idCliente);
				Garage garage = servicioGarage.buscarGarage(idGarage);
				if(auto1 !=null && cliente1 !=null && garage !=null) {
					
					modelo.put("auto", auto1);
					modelo.put("cliente", cliente1);
					modelo.put("ticket", ticket);
					modelo.put("garage", garage);
					
					return new ModelAndView("formularioReservaEstadia", modelo);
				}
		
			return new ModelAndView("formularioReservaEstadia", modelo);
				}
			
			}
		return new ModelAndView("redirect:/login");
	}	

	@RequestMapping(path="/realizarReservaEstadia/{cliente.id}/{auto.id}/{garage.id}", method=RequestMethod.GET)
	public ModelAndView procesarPagoEstadia(@RequestParam(value="fechaDesde")String fechaDesde,
									@RequestParam(value="fechaHasta")String fechaHasta,
									@PathVariable("cliente.id") Long idCliente,
									@PathVariable("auto.id") Long idAuto,
									@PathVariable("garage.id") Long idGarage,
									HttpServletRequest request){
		String rol = (String) request.getSession().getAttribute("roll");
		if(rol != null)
			if(rol.equals("cliente")) {
		ModelMap modelo = new ModelMap();
		Estacionamiento est = new Estacionamiento();
		Auto auto = servicioAuto.buscarAuto(idAuto);
		Cliente cliente = servicioCliente.consultarClientePorId(idCliente);
		Garage garage = servicioGarage.buscarGarage(idGarage);
						
		if(garage !=null && auto!=null && auto.getUsandoGarage().equals(false) && garage.getCapacidad()>garage.getContador()) {
			modelo.put("auto", auto);
			modelo.put("cliente", cliente);
			modelo.put("garage", garage);
			est.setFechaDesde(fechaDesde);
			est.setFechaHasta(fechaHasta);
			est.setGarage1(garage);
			
			servicioAuto.cambiarEstadoDeSiestaEnGarageOno(auto);
			//auto.setUsandoGarage(true);
			servicioGarage.sumarContador(garage);
			
			
			est.setAuto(auto);
			est.setGarage1(garage);
			
			
			Long dias = servicioCobrarTickets.calcularDias(fechaDesde, fechaHasta);
			Double precio = servicioCobrarTickets.calcularPrecioPorEstadia(garage.getPrecioEstadia(), fechaDesde, fechaHasta);
			
			est.setPrecioAPagar(precio);
			
			modelo.put("ticket", est);
			
			modelo.put("precio", precio);
			
			modelo.put("dias", dias);
			
			servicioCobrarTickets.registrarTicket(est);
			return new ModelAndView("pagarMontoEstadia", modelo);
		}
		return new ModelAndView("AlertaAutoEnGarage", modelo);
	
	
		}
	return new ModelAndView("redirect:/login");	
	}
	
	@RequestMapping(path="/pagarReservaEstadia/{cliente.id}/{estacionamiento.id}", method=RequestMethod.POST)
	public ModelAndView pagarReservaEstadia(@PathVariable("cliente.id") Long idCliente,
											@PathVariable("estacionamiento.id") Long idEstacionamiento) {
		ModelMap modelo = new ModelMap();
		Cliente cliente = servicioCliente.consultarClientePorId(idCliente);
		Billetera billetera = servicioBilletera.consultarBilleteraDeCliente(cliente);
		Estacionamiento estacionamiento = servicioEst.buscarEstacionamiento(idEstacionamiento);
		
		try {
			
			if(cliente != null && billetera != null && estacionamiento != null) {
				servicioBilletera.pagarReservaEstadia(estacionamiento, billetera);
				modelo.put("cliente", cliente.getNombre());
				modelo.put("billetera", billetera.getSaldo());
				modelo.put("estacionamiento", estacionamiento.getPrecioAPagar());
				return new ModelAndView("confirmacionReserva", modelo);
			}
		} catch(Exception e) {
			
			modelo.put("cliente", cliente);
			modelo.put("billetera", billetera);
			modelo.put("estacionamiento", estacionamiento);
			modelo.put("error", e.getMessage());
		}
		
		return null;
	}
	
	@RequestMapping(path="/mostrarFormularioReservaHora/{cliente.id}/{auto.id}/{garage.id}", method=RequestMethod.GET)
	public ModelAndView mostrarFormularioReservaHora(
			@PathVariable("cliente.id") Long idCliente,
			@PathVariable("auto.id") Long idAuto,
			@PathVariable("garage.id") Long idGarage, 
			HttpServletRequest request) {
		String rol = (String) request.getSession().getAttribute("roll");
		if(rol != null)
			if(rol.equals("cliente")) {
		
		
		ModelMap modelo = new ModelMap();
		Estacionamiento ticket = new Estacionamiento();
		Auto auto1 = servicioAuto.buscarAuto(idAuto);
		Cliente cliente1 = servicioCliente.consultarClientePorId(idCliente);
		Garage garage = servicioGarage.buscarGarage(idGarage);
		
			if(auto1 !=null && cliente1 !=null && garage !=null) {
				
				modelo.put("auto", auto1);
				modelo.put("cliente", cliente1);
				modelo.put("ticket", ticket);
				modelo.put("garage", garage);
				
				return new ModelAndView("formularioReservaHora", modelo);
			}
	
		return new ModelAndView("formularioReservaHora", modelo);
			}
		return new ModelAndView("redirect:/login");
	}
	
	@RequestMapping(path="/realizarReservaHora/{cliente.id}/{auto.id}/{garage.id}")
	public ModelAndView procesarPagoHora(@RequestParam(value="horaDesde")String horaDesde,
									@RequestParam(value="horaHasta")String horaHasta,
									@PathVariable("cliente.id") Long idCliente,
									@PathVariable("auto.id") Long idAuto,
									@PathVariable("garage.id") Long idGarage,
									HttpServletRequest request
									){
		
		
		String rol = (String) request.getSession().getAttribute("roll");
		if(rol != null)
			if(rol.equals("cliente")) {
		ModelMap modelo = new ModelMap();
		Estacionamiento est = new Estacionamiento();
		Auto auto = servicioAuto.buscarAuto(idAuto);
		Cliente cliente = servicioCliente.consultarClientePorId(idCliente);
		Garage garage = servicioGarage.buscarGarage(idGarage);
		                                       //Esto le puse Nuevo
			if(garage !=null && auto!=null && auto.getUsandoGarage().equals(false) && garage.getCapacidad()>garage.getContador()) {
				modelo.put("auto", auto);
				modelo.put("cliente", cliente);
				modelo.put("garage", garage);
				est.setHoraDesde(horaDesde);
				est.setHoraHasta(horaHasta);
				est.setGarage1(garage);
				
				servicioAuto.cambiarEstadoDeSiestaEnGarageOno(auto);
				servicioGarage.sumarContador(garage);
				//auto.setUsandoGarage(true);
				
				est.setAuto(auto);
				est.setGarage1(garage);
				
				Long horas = servicioCobrarTickets.calcularHoras(est.getHoraDesde(), est.getHoraHasta());
				Double precio = servicioCobrarTickets.calcularPrecioPorHora(garage.getPrecioHora(), horaDesde, horaHasta);
				
				est.setPrecioAPagar(precio);
				
				modelo.put("ticket", est);
				
				modelo.put("precio", precio);
				
				modelo.put("horas", horas);
				
				servicioCobrarTickets.registrarTicket(est);
				
				return new ModelAndView("pagarMontoHora", modelo);
			}
			return new ModelAndView("AlertaAutoEnGarage", modelo);
		
		
			}
		return new ModelAndView("redirect:/login");	
			
}
}
