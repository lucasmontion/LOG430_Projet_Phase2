package ca.ets.log430.candronex.commun.adapter.in.web;

/** Format unique des erreurs retournées par l'API. */
public record ErreurResponse(String code, String message) {}
