package application;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dim_itemValue {
	private StringProperty item;
	private DoubleProperty P2;
	private DoubleProperty F;
	private DoubleProperty E;

	public Dim_itemValue(String _item,double _P2,double _F,double _E) {
		item = new SimpleStringProperty(_item);
		P2 = new SimpleDoubleProperty(_P2);
		F = new SimpleDoubleProperty(_F);
		E = new SimpleDoubleProperty(_E);
	}

	public StringProperty itemProperty() {
		return item;
	}

	public DoubleProperty P2Property() {
		return P2;
	}
	public DoubleProperty FProperty() {
		return F;
	}
	public DoubleProperty EProperty() {
		return E;
	}
}
