-- Run once against PostgreSQL after the application has created these tables.
-- Protects financial history from accidental UPDATE/DELETE through ordinary app SQL.
CREATE OR REPLACE FUNCTION prevent_ledgerline_history_mutation()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'LedgerLine history is append-only: % is not permitted on %', TG_OP, TG_TABLE_NAME;
END;
$$;

DROP TRIGGER IF EXISTS ledger_entries_no_update_delete ON ledger_entries;
CREATE TRIGGER ledger_entries_no_update_delete
BEFORE UPDATE OR DELETE ON ledger_entries
FOR EACH ROW EXECUTE FUNCTION prevent_ledgerline_history_mutation();

DROP TRIGGER IF EXISTS audit_events_no_update_delete ON audit_events;
CREATE TRIGGER audit_events_no_update_delete
BEFORE UPDATE OR DELETE ON audit_events
FOR EACH ROW EXECUTE FUNCTION prevent_ledgerline_history_mutation();
