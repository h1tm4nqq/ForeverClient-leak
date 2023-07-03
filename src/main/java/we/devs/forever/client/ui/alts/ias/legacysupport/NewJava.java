package we.devs.forever.client.ui.alts.ias.legacysupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class NewJava
        implements ILegacyCompat {
    @Override
    public int[] getDate() {
        int[] ret = new int[]{LocalDateTime.now().getMonthValue(), LocalDateTime.now().getDayOfMonth(), LocalDateTime.now().getYear()};
        return ret;
    }

    @Override
    public String getFormattedDate() {
        DateTimeFormatter format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        LocalDate date = LocalDateTime.now().withDayOfMonth(this.getDate()[1]).withMonth(this.getDate()[0]).withYear(this.getDate()[2]).toLocalDate();
        return date.format(format);
    }
}

