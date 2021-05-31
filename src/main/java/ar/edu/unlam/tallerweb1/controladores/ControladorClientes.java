package ar.edu.unlam.tallerweb1.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.mysql.cj.xdevapi.SessionFactory;

import ar.edu.unlam.tallerweb1.modelo.Auto;
import ar.edu.unlam.tallerweb1.modelo.Cliente;
import ar.edu.unlam.tallerweb1.servicios.ServicioAuto;
import ar.edu.unlam.tallerweb1.servicios.ServicioCliente;
import ar.edu.unlam.tallerweb1.servicios.ServicioLogin;

@Controller
public class ControladorClientes {
	
	private ServicioLogin servicioLogin;
	private ServicioAuto servicioAuto;
	
	@Autowired
	private ControladorClientes(ServicioLogin servicioLogin,ServicioAuto servicioAuto) {
		this.servicioAuto =servicioAuto;
		this.servicioLogin = servicioLogin;
	}
	
	@RequestMapping(path="/mostrarClientes", method=RequestMethod.GET)
		public String clientes(Model modelo) {
			modelo.addAttribute("clientes", servicioLogin.listaDeClientes());
			return("ListaClientes");
	}
	@RequestMapping(path="/mostrarAutosClientes/{id}", method=RequestMethod.GET)
	public ModelAndView AutosDeClientes(
			@PathVariable("id")Long id) {
		ModelMap modelo = new ModelMap();
		Cliente cliente = servicioLogin.consultarClientePorId(id);
		modelo.put("auto",servicioAuto.consultarAutoDeCliente(cliente) );
		return new ModelAndView("ListaAutosDeCliente", modelo);
}
			
	
		
	

}
