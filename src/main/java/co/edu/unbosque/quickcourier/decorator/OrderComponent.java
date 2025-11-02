package co.edu.unbosque.quickcourier.decorator;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface Component para el patrón Decorator
 * Define las operaciones que pueden ser decoradas con extras
 */
public interface OrderComponent {

    /**
     * Obtiene el costo total del pedido incluyendo todos los extras aplicados
     *
     * @return Costo total
     */
    BigDecimal getCost();

    /**
     * Obtiene la descripción completa del pedido con todos los extras
     *
     * @return Descripción detallada
     */
    String getDescription();

    /**
     * Obtiene la lista de códigos de extras aplicados
     *
     * @return Lista de códigos de extras
     */
    List<String> getAppliedExtras();

    /**
     * Obtiene el peso total del pedido
     * Algunos extras pueden afectar el peso (ej: empaque especial)
     *
     * @return Peso en kilogramos
     */
    BigDecimal getWeight();
}