# cars-service: a simple REST service using play & slick

## API

<table>
  <tr>
    <td> Method </td> <td> Path </td> <td> Data </td> <td> Responses </td>
  </tr>
  <tr>
    <td>GET</td>
    <td><pre>/cars?sort={field_name}&desc={false|true}</pre></td>
    <td></td>
    <td>200 - list of cars</td>
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

