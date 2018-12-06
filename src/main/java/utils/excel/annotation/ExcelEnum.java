package utils.excel.annotation;

public enum ExcelEnum {

    ExportData("EXCEL导出数据", 1),
    ExportTemplate("EXCEL导出模板", 2);

    private String display;
    private int index;

    ExcelEnum(String display) {
        this.display = display;
    }

    ExcelEnum(String display, int index) {
        this.display = display;
        this.index = index;
    }

    public String display() {
        return this.display;
    }

    public int getIndex() {
        return this.index;
    }

    public static ExcelEnum fromName(String name) {
        for (ExcelEnum orderTypeEnum : values()) {
            if (orderTypeEnum.name().equalsIgnoreCase(name)) {
                return orderTypeEnum;
            }
        }
        return null;
    }


}
