package com.securboration.immortals.instantiation.delete;
//package com.securboration.immortals.instantiation;
//
//public class TestEntryPoint {
//    
//    public static enum Enumeration{
//        ENUM_A,ENUM_b,ENUM_c,ENUM_d,ENUM_e,ENUM_F,ENUM_G,ENUM_h,ENUM_i,ENUM_j,ENUM_K,ENUM_L,ENUM_m,ENUM_n;
//    }
//    
//    public static Object[] getObjects(){
//        
//        Object a = new A(1,"tagA",new Object[]{1,"2",'3',4d});
//        Object b = new B(5d,"tagB");
//        Object c = new C(1,"tagC",new Object[]{a,b,Enumeration.ENUM_h});
//        Object d = new A(1,"tagD",new Object[]{a,b,new Object(),Enumeration.ENUM_A,Enumeration.ENUM_b,"test",4,'q'});
//        
//        return new Object[]{
//                a,b,c,d
//        };
//        
//    }
//    
//    public static class A{
//        private Enumeration enumeration = Enumeration.ENUM_n;
//        private int id;
//        private String tag;
//        private Object[] values;
//        private byte[] bytes = "the yellow dog".getBytes();
//        
//        private A(int id, String tag, Object[] values) {
//            super();
//            this.id = id;
//            this.tag = tag;
//            this.values = values;
//        }
//    }
//    
//    public static class B{
//        private double f;
//        private String tag;
//        public B(double f, String tag) {
//            super();
//            this.f = f;
//            this.tag = tag;
//        }
//    }
//    
//    public static class C extends A{
//        private String tag;
//        
//        private C(int id, String tag, Object[] values) {
//            super(id,tag,values);
//            this.tag = tag + "notDuplicated";
//        }
//    }
//
//}
