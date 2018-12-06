import org.junit.Test;
import utils.excel.annotation.ExcelEnum;

public class EnumTest {

    @Test
    public void test1(){

        System.out.println(ExcelEnum.ExportData.name());
        System.out.println(ExcelEnum.ExportData.ordinal());
        System.out.println(ExcelEnum.ExportData.getIndex());

    }

}
