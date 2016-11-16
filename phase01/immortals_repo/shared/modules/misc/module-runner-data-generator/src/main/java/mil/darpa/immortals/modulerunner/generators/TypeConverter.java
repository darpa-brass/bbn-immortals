package mil.darpa.immortals.modulerunner.generators;

/**
 * Created by awellman@bbn.com on 8/8/16.
 */
public abstract class TypeConverter<InputType, OutputType> {

    public abstract OutputType convert(InputType input);

    public static TypeConverter ConstructTypeConverter(JavaClassType inputType, JavaClassType outputType) {
        if (inputType == JavaClassType.String) {
            if (outputType == JavaClassType.ByteArray) {

                return new TypeConverter<String, byte[]>() {
                    @Override
                    public byte[] convert(String input) {
                        return TypeConverter.stringToBytes(input);
                    }
                };

            } else {
                throw new RuntimeException("Cannot convert '" + inputType + "' to String!");
            }

        } else if (inputType == JavaClassType.ByteArray) {

            if (outputType == JavaClassType.String) {

                return new TypeConverter<byte[], String>() {
                    @Override
                    public String convert(byte[] input) {
                        return TypeConverter.bytesToString(input);
                    }
                };

            } else {
                throw new RuntimeException("Cannot convert '" + inputType + "' to byte[]!");
            }

        } else {
            throw new RuntimeException("No converters exist for '" + inputType + "'!");
        }
    }

    private TypeConverter() {

    }

//    private static boolean isString(Class inputType) {
//        return inputType == String.class;
//    }
//
//    private static boolean isByteArray(Class inputType) {
//        return inputType.getComponentType().isPrimitive() && byte.class.isAssignableFrom(inputType.getComponentType());
//    }

    public static byte[] stringToBytes(String str) {
        return str.getBytes();
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes);
    }
}
