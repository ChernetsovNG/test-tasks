package ru.nchernetsov.test.bks.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.nchernetsov.test.bks.domain.StockPacketExt;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockPacketExtError extends StockPacketExt {
}
