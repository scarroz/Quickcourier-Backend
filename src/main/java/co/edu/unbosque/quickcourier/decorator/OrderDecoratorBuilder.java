package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingExtra;
import co.edu.unbosque.quickcourier.repository.ShippingExtraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Builder para construir cadenas de decoradores de manera fluida
 * Facilita la adicion de muuuuultiples extras a un pedido
 */
@Component
public class OrderDecoratorBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OrderDecoratorBuilder.class);

    private final ShippingExtraRepository shippingExtraRepository;

    // Mapa de codigos de extra a funciones constructoras de los deocradores
    private final Map<String, BiFunction<OrderComponent, ShippingExtra, OrderDecorator>> decoratorFactories;

    public OrderDecoratorBuilder(ShippingExtraRepository shippingExtraRepository) {
        this.shippingExtraRepository = shippingExtraRepository;
        this.decoratorFactories = new HashMap<>();

        //
        decoratorFactories.put("EXPRESS", ExpressDecorator::new);
        decoratorFactories.put("FRAGILE", FragileDecorator::new);
        decoratorFactories.put("INSURANCE", InsuranceDecorator::new);
        decoratorFactories.put("GIFT_WRAP", GiftWrapDecorator::new);
        decoratorFactories.put("CARBON_NEUTRAL", CarbonNeutralDecorator::new);

        logger.info("OrderDecoratorBuilder initialized with {} decorator types",
                decoratorFactories.size());
    }

    /**
     * Construye una cadena de decoradores aplicando los extras especificados
     *
     * @param order Pedido base
     * @param extraCodes Códigos de extras a aplicar
     * @return OrderComponent decorado con todos los extras
     */
    public OrderComponent buildWithExtras(Order order, List<String> extraCodes) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (extraCodes == null || extraCodes.isEmpty()) {
            logger.debug("No extras to apply for order {}", order.getOrderNumber());
            return new BaseOrder(order);
        }

        logger.debug("Building order {} with {} extras: {}",
                order.getOrderNumber(), extraCodes.size(), extraCodes);

        // Comenzar con el pedido base
        OrderComponent decoratedOrder = new BaseOrder(order);

        // Obtener los extras de la base de datos
        List<ShippingExtra> extras = shippingExtraRepository.findByCodesAndActive(extraCodes);

        if (extras.size() != extraCodes.size()) {
            logger.warn("Some extras not found or inactive. Requested: {}, Found: {}",
                    extraCodes.size(), extras.size());
        }

        // Aplicar cada extra como un decorador
        for (ShippingExtra extra : extras) {
            decoratedOrder = applyDecorator(decoratedOrder, extra);
        }

        logger.info("Order {} decorated with {} extras. Final cost: ${}",
                order.getOrderNumber(), extras.size(), decoratedOrder.getCost());

        return decoratedOrder;
    }

    /**
     * Aplica un decorador específico basado en el código del extra
     */
    private OrderDecorator applyDecorator(OrderComponent wrappedOrder, ShippingExtra extra) {
        BiFunction<OrderComponent, ShippingExtra, OrderDecorator> factory =
                decoratorFactories.get(extra.getCode());

        if (factory == null) {
            logger.warn("No decorator factory found for extra code: {}. Using generic decorator.",
                    extra.getCode());
            return new GenericDecorator(wrappedOrder, extra);
        }

        OrderDecorator decorator = factory.apply(wrappedOrder, extra);
        logger.debug("Applied {} decorator for extra: {}",
                decorator.getClass().getSimpleName(), extra.getCode());

        return decorator;
    }

    /**
     * Decorador genérico para extras sin decorador específico
     */
    private static class GenericDecorator extends OrderDecorator {

        GenericDecorator(OrderComponent wrappedOrder, ShippingExtra shippingExtra) {
            super(wrappedOrder, shippingExtra);
        }

        @Override
        protected String getExtraDescription() {
            return String.format("%s +$%s",
                    shippingExtra.getName(), getExtraCost());
        }
    }

    /**
     * Registra un nuevo tipo de decorador
     * Útil para extensibilidad
     */
    public void registerDecorator(String extraCode,
                                  BiFunction<OrderComponent, ShippingExtra, OrderDecorator> factory) {
        decoratorFactories.put(extraCode, factory);
        logger.info("Registered new decorator for extra code: {}", extraCode);
    }

    /**
     * Obtiene los códigos de extras soportados
     */
    public java.util.Set<String> getSupportedExtraCodes() {
        return decoratorFactories.keySet();
    }
}