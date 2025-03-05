"""Password generation for WobblyByte.

The Android app calls this module through Chaquopy; the tests import it
directly. It uses only the standard library, and every random choice comes
from `secrets`, which reads the operating system's CSPRNG.
"""

import json
import math
import secrets
import string
import time

LOWER = string.ascii_lowercase
UPPER = string.ascii_uppercase
DIGITS = string.digits
SYMBOLS = "!@#$%^&*()-_=+[]{};:,.?"
AMBIGUOUS = frozenset("Il1O0o")

MIN_LENGTH = 8
MAX_LENGTH = 64

_shuffler = secrets.SystemRandom()


def _pools(lower, upper, digits, symbols, exclude_ambiguous):
    """Return one string of allowed characters per selected character type."""
    selected = []
    for enabled, characters in (
        (lower, LOWER),
        (upper, UPPER),
        (digits, DIGITS),
        (symbols, SYMBOLS),
    ):
        if not enabled:
            continue
        if exclude_ambiguous:
            characters = "".join(c for c in characters if c not in AMBIGUOUS)
        selected.append(characters)
    return selected


def _draw(length, pools):
    if not MIN_LENGTH <= length <= MAX_LENGTH:
        raise ValueError(
            "length must be between %d and %d" % (MIN_LENGTH, MAX_LENGTH)
        )
    if not pools:
        raise ValueError("choose at least one character type")

    # One guaranteed character per selected type, so a short password never
    # misses a type the user asked for. The rest come from the combined set.
    chars = [secrets.choice(pool) for pool in pools]
    everything = "".join(pools)
    chars.extend(secrets.choice(everything) for _ in range(length - len(chars)))
    _shuffler.shuffle(chars)
    return "".join(chars)


def generate(
    length=16,
    lower=True,
    upper=True,
    digits=True,
    symbols=False,
    exclude_ambiguous=False,
):
    """Return a random password.

    `lower` and `upper` control small and capital letters separately, which is
    how the app exposes capitalization.
    """
    return _draw(length, _pools(lower, upper, digits, symbols, exclude_ambiguous))


def estimate_entropy_bits(length, pool_size):
    """Entropy of a uniformly random string. The one-of-each-type rule lowers
    the true figure slightly, so treat this as an estimate."""
    if pool_size < 2:
        return 0.0
    return length * math.log2(pool_size)


def strength_label(entropy_bits):
    if entropy_bits < 40:
        return "weak"
    if entropy_bits < 60:
        return "fair"
    if entropy_bits < 80:
        return "strong"
    return "very strong"


def generate_with_meta(
    length=16,
    lower=True,
    upper=True,
    digits=True,
    symbols=False,
    exclude_ambiguous=False,
):
    """Generate a password and return it with its details as a JSON string.

    The Java side parses this, so the key names are part of the contract:
    password, length, pool_size, entropy_bits, strength, python_ms.
    """
    started = time.perf_counter()
    pools = _pools(lower, upper, digits, symbols, exclude_ambiguous)
    password = _draw(length, pools)
    pool_size = sum(len(pool) for pool in pools)
    bits = estimate_entropy_bits(len(password), pool_size)
    elapsed_ms = (time.perf_counter() - started) * 1000.0
    return json.dumps(
        {
            "password": password,
            "length": len(password),
            "pool_size": pool_size,
            "entropy_bits": round(bits, 1),
            "strength": strength_label(bits),
            "python_ms": round(elapsed_ms, 3),
        }
    )
