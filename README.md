# cars-service: a simple REST service using play & slick

## Setup

- Build service image using <code> Docker / publishLocal </code> in sbt console
- Run <code>docker-compose up</code> from cars-service directory. Service would be available on <code>localhost:9000</code>

## API

<table>
  <tr>
    <td> Method </td> <td> Path </td> <td> Data </td> <td> Responses </td>
  </tr>
  <tr>
    <td>GET</td>
    <td><pre>/cars[?sort={field_name}[&desc=true}][&{field_name}={filter}]</pre></td>
    <td></td>
    <td>
	    200 - list of cars
	    <br>
	    400 - invalid sort parameter
    </td>
  </tr>
  <tr>
    <td>GET</td>
    <td><pre>/car/{id}</pre></td>
    <td></td>
    <td>
	    200 - car entry
	<br>
	    404 - no car for given id
    </td>	  
  </tr>
  <tr>
    <td>POST</td>
    <td><pre>/car</pre></td>
    <td>
      
```json
{
	"registration_number": "some_number_1",
	"make": "kia",
	"model": "rio",
	"color": "green",
	"manufacturing_year": 2010
}
```
  </td>
    <td>
	    201 - id of created car entry
	    <br>
	    400 - duplicate registration_number error
    </td>
  </tr>
  <tr>
    <td>PUT</td>
    <td><pre>/car</pre></td>
    <td>
      
```json
{
  	"id": "1",
	"registration_number": "some_number_1",
	"make": "kia",
	"model": "rio",
	"color": "green",
	"manufacturing_year": 2010
}
```
  </td>
    <td>
	204 - car entry is successfully updated
	<br>
	400 - duplicate registration_number error
    </td>
  </tr>
  </tr>
  <tr>
    <td>DELETE</td>
    <td><pre>/car/{id}</pre></td>
    <td></td>
    <td>
	204 - car entry is successfully removed
	<br>
	404 - no car for given id
    </td>
  </tr>
  </tr>
  <tr>
    <td>GET</td>
    <td><pre>/cars/statistics</pre></td>
    <td></td>
    <td>
	    200 - statistics object
    </td>
  </tr>
</table>

## App Overview
- **model**: contains classes for car entry and statistic object
- **controllers**: contains a single CarsController
- **repositories**: contain CarRepository and CarStatisticsRepository traits to abstract persistence layer in controller
- **persistence.db** contain persistence logic implemented with Slick
- **persistence.inmemory** repositories backed by ListBuffer, used on early impementation stages
- **modules** DI (guice) modules, DebugModule for inmemory implementation and DbModule for DB (Slick) implementation
