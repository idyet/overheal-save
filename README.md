# Overheal Save

A RuneLite plugin that warns when your overheal HP is about to decay, so you can flick Rapid Heal to reset the timer and keep the extra hitpoints.

## Behavior

When you are healed above your max HP (e.g. from a Saradomin brew or anglerfish), your HP decays by 1 every 100 game ticks (60 seconds). Toggling the Rapid Heal prayer resets that cycle, letting you preserve overheal indefinitely as long as you flick it before each decay tick.

This plugin tracks the decay cycle and surfaces three signals while overhealed:

- **HP orb progress arc** — a red arc around the hitpoints orb that shrinks toward the next decay tick. Matches the Regeneration Meter style and renders on top of it.
- **Rapid Heal highlight** — the same arc drawn around the Rapid Heal prayer icon in the prayer book.
- **Notification** — a configurable RuneLite notification fires once per cycle when the arc enters the warning window.

The arc flashes red and orange during the warning lead, then resets after Rapid Heal is flicked or the decay tick fires.

## Configuration

| Option | Description | Default |
|--------|-------------|---------|
| Notification sound | Fire a RuneLite notification when overheal decay is imminent | Disabled |
| Flash HP orb | Draw the decay arc around the hitpoints orb | Enabled |
| Highlight Rapid Heal | Draw the decay arc around the Rapid Heal prayer icon | Enabled |
| Warning lead (ticks) | Game ticks before the decay tick to start warning (1 tick = 0.6s) | 25 |
