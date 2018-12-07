import org.junit.Test;
import utils.excel.annotation.ExcelConfEnum;

public class EnumTest {

    @Test
    public void test1(){

        System.out.println(ExcelConfEnum.ExportData.name());
        System.out.println(ExcelConfEnum.ExportData.ordinal());
        System.out.println(ExcelConfEnum.ExportData.getIndex());

    }

}
