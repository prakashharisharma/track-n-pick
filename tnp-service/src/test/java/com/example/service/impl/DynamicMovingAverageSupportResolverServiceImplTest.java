package com.example.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.MAEvaluationResult;
import com.example.service.MovingAverageLength;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DynamicMovingAverageSupportResolverServiceImplTest {

    @InjectMocks private DynamicMovingAverageSupportResolverServiceImpl service;

    @Mock private StockPrice stockPrice;

    @Mock private StockTechnicals stockTechnicals;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private MAEvaluationResult result(
            MovingAverageLength len,
            boolean support,
            boolean nearSupport,
            boolean breakout,
            boolean nearResistance,
            boolean breakdown) {
        return new MAEvaluationResult(
                len, 100.0, support, nearSupport, breakout, nearResistance, breakdown);
    }

    @Test
    void testBreakdownPlusSupport() {
        var breakdown = result(MovingAverageLength.MEDIUM, false, false, false, false, true);
        var support = result(MovingAverageLength.LOW, true, true, false, false, false);

        var spy = spy(service);
        doReturn(List.of(breakdown, support)).when(spy).evaluateInteractions(any(), any(), any());

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isPresent());
        assertEquals(support, result.get());
    }

    @Test
    void testBreakoutPlusResistance() {
        var breakout = result(MovingAverageLength.MEDIUM, false, false, true, false, false);
        var resistance = result(MovingAverageLength.HIGH, false, false, false, true, false);

        var spy = spy(service);
        doReturn(List.of(breakout, resistance)).when(spy).evaluateInteractions(any(), any(), any());

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isPresent());
        assertEquals(resistance, result.get());
    }

    @Test
    void testMultipleBreakouts() {
        var breakout1 = result(MovingAverageLength.LOW, false, false, true, false, false);
        var breakout2 = result(MovingAverageLength.MEDIUM, false, false, true, false, false);

        var spy = spy(service);
        doReturn(List.of(breakout1, breakout2)).when(spy).evaluateInteractions(any(), any(), any());

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isPresent());
        assertEquals(breakout1, result.get());
    }

    @Test
    void testMultipleBreakdowns() {
        var breakdown1 = result(MovingAverageLength.HIGH, false, false, false, false, true);
        var breakdown2 = result(MovingAverageLength.LOW, false, false, false, false, true);

        var spy = spy(service);
        doReturn(List.of(breakdown1, breakdown2))
                .when(spy)
                .evaluateInteractions(any(), any(), any());

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isPresent());
        assertEquals(breakdown1, result.get());
    }

    @Test
    void testMultipleSupports() {
        var support1 = result(MovingAverageLength.HIGH, true, true, false, false, false);
        var support2 = result(MovingAverageLength.LOW, true, true, false, false, false);

        var spy = spy(service);
        doReturn(List.of(support1, support2)).when(spy).evaluateInteractions(any(), any(), any());

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isPresent());
        assertEquals(support1, result.get());
    }

    @Test
    void testMultipleResistances() {
        var res1 = result(MovingAverageLength.HIGH, false, false, false, true, false);
        var res2 = result(MovingAverageLength.LOW, false, false, false, true, false);

        var spy = spy(service);
        doReturn(List.of(res1, res2)).when(spy).evaluateInteractions(any(), any(), any());

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isPresent());
        assertEquals(res2, result.get());
    }

    @Test
    void testFallbackSingleSignal() {
        var res = result(MovingAverageLength.MEDIUM, true, true, false, false, false);

        var spy = spy(service);
        doReturn(List.of(res)).when(spy).evaluateInteractions(any(), any(), any());
        // doReturn(100).when(spy).calculateSignalScore(res);

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isPresent());
        assertEquals(res, result.get());
    }

    @Test
    void testNoSignals() {
        var invalid =
                new MAEvaluationResult(
                        MovingAverageLength.HIGH, 100.0, false, false, false, false, false);

        var spy = spy(service);
        doReturn(List.of(invalid)).when(spy).evaluateInteractions(any(), any(), any());

        Optional<MAEvaluationResult> result =
                spy.evaluateSingleInteractionSmart(Timeframe.DAILY, stockPrice, stockTechnicals);
        assertTrue(result.isEmpty());
    }
}
