# cars-service: a simple REST service using play & slick

## API

<table>
  <tr>
    <td> Method </td> <td> Path </td> <td> Data </td>
  </tr>
  <tr>
    <td>GET</td>
    <td><pre>/cars?sort={field_name}&desc={false|true}</pre></td>
    <td></td>
  </tr>
  <tr>
    <td>GET</td>
    <td><pre>/car/{id}</pre></td>
    <td></td>
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
  </tr>
  </tr>
  <tr>
    <td>DELETE</td>
    <td><pre>/car/{id}</pre></td>
    <td></td>
  </tr>
  </tr>
  <tr>
    <td>GET</td>
    <td><pre>/cars/statistics</pre></td>
    <td></td>
  </tr>
</table>

