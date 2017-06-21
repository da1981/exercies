package com.rest.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeRESTController {
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private final Validator validator;

	public EmployeeRESTController(Validator validator) {
		this.validator = validator;
	}

	@GET
	public Response getEmployees(@QueryParam("format") String format) {
		return Response.ok(EmployeeDB.getEmployees()).build();
	}

	@GET
	@Path("/{id}")
	public Response getEmployeeById(@PathParam("id") Integer id) {
		Employee employee = EmployeeDB.getEmployee(id);
		if (employee != null)
			return Response.ok(employee).build();
		else
			return Response.status(Status.NOT_FOUND).build();
	}

	@POST
	public Response createEmployee(String content) throws URISyntaxException {
		LOGGER.info("POST");
		ObjectMapper mapper = new ObjectMapper();
		Employee employee = null;
		try {
			employee = mapper.readValue(content, Employee.class);
		} catch (JsonParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // deserializes json into  employee
	
		// validation
		Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
		Employee e = EmployeeDB.getEmployee(employee.getId());
		if (violations.size() > 0) {
			ArrayList<String> validationMessages = new ArrayList<String>();
			for (ConstraintViolation<Employee> violation : violations) {
				validationMessages.add(violation.getPropertyPath().toString() + ": " + violation.getMessage());
			}
			return Response.status(Status.BAD_REQUEST).entity(validationMessages).build();
		}
		if (e != null) {
			EmployeeDB.updateEmployee(employee.getId(), employee);
			return Response.created(new URI("/employees/" + employee.getId())).build();
		} else
			return Response.status(Status.NOT_FOUND).build();
	}

	@PUT
	@Path("/{id}")
	//@Consumes(MediaType.APPLICATION_JSON)
	public Response updateEmployeeById(@PathParam("id") Integer id, Employee employee) {
		LOGGER.info("PUT");
		// validation
		Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
		Employee e = EmployeeDB.getEmployee(employee.getId());
		if (violations.size() > 0) {
			ArrayList<String> validationMessages = new ArrayList<String>();
			for (ConstraintViolation<Employee> violation : violations) {
				validationMessages.add(violation.getPropertyPath().toString() + ": " + violation.getMessage());
			}
			return Response.status(Status.BAD_REQUEST).entity(validationMessages).build();
		}
		if (e != null) {
			employee.setId(id);
			EmployeeDB.updateEmployee(id, employee);
			return Response.ok(employee).build();
		} else
			return Response.status(Status.NOT_FOUND).build();
	}

	@DELETE
	@Path("/{id}")
	public Response removeEmployeeById(@PathParam("id") Integer id) {
		Employee employee = EmployeeDB.getEmployee(id);
		if (employee != null) {
			EmployeeDB.removeEmployee(id);
			return Response.ok().build();
		} else
			return Response.status(Status.NOT_FOUND).build();
	}
}
