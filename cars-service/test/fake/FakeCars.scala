package fake

import models.Car

object FakeCars {
  val fakeCars = List(
    Car(0, "number1"),
    Car(0, "number2", make = Some("Toyota"), manufacturingYear = Some(1999)),
    Car(0, "number3", make = Some("Kia"), model = Some("Rio"), color = Some("Black"), manufacturingYear = Some(2010)),
    Car(0, "number4", make = Some("Toyota"), model = Some("Corolla"), color = Some("Green"), manufacturingYear = Some(2001)),
    Car(0, "number5", make = Some("Kia"), model = Some("Soul"), color = Some("White"), manufacturingYear = Some(2010)),
    Car(0, "number6", make = Some("Honda"), model = Some("Civic"), color = Some("Green"), manufacturingYear = Some(2005))
  )
}
