package reflect;

/**
 * Created by 11256 on 2018/8/24.
 * 注解3
 */
@interface MyAnnotation3 {

    int value();
    String name() default "注解3";

}
