package utils.excel.annotation;

public enum ExcelConfEnum {

    ExportData("EXCEL导出数据", 1),
    ExportTemplate("EXCEL导出模板", 2),
    rowAccessWindowSize("指定的内存中缓存记录数", 500),
    sheetName("sheet页名称", "exportd");

    private String display;
    private int index;
    private String desc;

    ExcelConfEnum(String display, int index) {
        this.display = display;
        this.index = index;
    }

    ExcelConfEnum(String display, String desc) {
        this.display = display;
        this.desc = desc;
    }


    public String display() {
        return this.display;
    }

    public int getIndex() {
        return this.index;
    }

    public String getDesc() {
        return this.desc;
    }

    public static ExcelConfEnum fromName(String name) {
        for (ExcelConfEnum orderTypeEnum : values()) {
            if (orderTypeEnum.name().equalsIgnoreCase(name)) {
                return orderTypeEnum;
            }
        }
        return null;
    }


}
