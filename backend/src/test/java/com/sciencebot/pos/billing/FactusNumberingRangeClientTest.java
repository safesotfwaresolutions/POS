package com.sciencebot.pos.billing;

import com.sciencebot.pos.billing.internal.adapters.factus.FactusClientHelper;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusNumberingRangeClient;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusNumberingRangeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class FactusNumberingRangeClientTest {

    @Mock
    private FactusClientHelper clientHelper;

    private FactusNumberingRangeMapper rangeMapper;
    private FactusNumberingRangeClient rangeClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rangeMapper = new FactusNumberingRangeMapper();
        rangeClient = new FactusNumberingRangeClient(clientHelper, rangeMapper);
    }

    @Test
    void getProviderName_ReturnsFactus() {
        assertEquals("factus", rangeClient.getProviderName());
    }

    @Test
    void numberingRanges_NullArguments_ThrowsException() {
        assertThrows(NullPointerException.class, () -> rangeClient.getNumberingRange(null));
        assertThrows(NullPointerException.class, () -> rangeClient.createNumberingRange(null));
        assertThrows(NullPointerException.class, () -> rangeClient.deleteNumberingRange(null));
        assertThrows(NullPointerException.class, () -> rangeClient.toggleNumberingRangeStatus(null));
    }
}
