import json
import math
import string
import time

import pytest

from wobblebyte import generator


@pytest.mark.parametrize("length", [8, 16, 32, 64])
def test_length_is_exact(length):
    assert len(generator.generate(length=length)) == length


def test_every_selected_type_appears_even_at_minimum_length():
    for _ in range(300):
        password = generator.generate(length=8, symbols=True)
        assert any(c in string.ascii_lowercase for c in password)
        assert any(c in string.ascii_uppercase for c in password)
        assert any(c in string.digits for c in password)
        assert any(c in generator.SYMBOLS for c in password)


def test_only_selected_types_are_used():
    for _ in range(100):
        password = generator.generate(length=24, lower=False, upper=False, digits=True)
        assert set(password) <= set(string.digits)


def test_capitalization_toggle():
    for _ in range(100):
        no_caps = generator.generate(length=20, upper=False)
        assert not any(c in string.ascii_uppercase for c in no_caps)

        only_caps = generator.generate(length=20, lower=False)
        assert not any(c in string.ascii_lowercase for c in only_caps)
        assert any(c in string.ascii_uppercase for c in only_caps)


def test_numbers_toggle():
    for _ in range(100):
        assert not any(c in string.digits for c in generator.generate(length=20, digits=False))


def test_exclude_ambiguous_removes_lookalikes():
    for _ in range(200):
        password = generator.generate(length=64, symbols=True, exclude_ambiguous=True)
        assert not set(password) & generator.AMBIGUOUS


@pytest.mark.parametrize("length", [-1, 0, 7, 65, 1000])
def test_length_out_of_range_is_rejected(length):
    with pytest.raises(ValueError):
        generator.generate(length=length)


def test_at_least_one_type_is_required():
    with pytest.raises(ValueError):
        generator.generate(lower=False, upper=False, digits=False, symbols=False)


def test_passwords_do_not_repeat():
    batch = {generator.generate(length=16) for _ in range(2000)}
    assert len(batch) == 2000


def test_meta_has_the_keys_the_java_side_reads():
    meta = json.loads(generator.generate_with_meta(16, True, True, True, False, False))
    assert set(meta) == {
        "password",
        "length",
        "pool_size",
        "entropy_bits",
        "strength",
        "python_ms",
    }
    assert meta["length"] == len(meta["password"]) == 16
    assert meta["pool_size"] == 62


def test_entropy_estimate_matches_the_formula():
    meta = json.loads(generator.generate_with_meta(16, True, True, True, False, False))
    assert meta["entropy_bits"] == round(16 * math.log2(62), 1)
    assert meta["strength"] == "very strong"


def test_ambiguous_exclusion_shrinks_the_pool():
    meta = json.loads(generator.generate_with_meta(16, True, True, True, False, True))
    # 62 characters minus I, l, 1, O, 0, o
    assert meta["pool_size"] == 56


@pytest.mark.parametrize(
    "bits, label",
    [(0, "weak"), (39.9, "weak"), (40, "fair"), (59.9, "fair"), (60, "strong"), (79.9, "strong"), (80, "very strong")],
)
def test_strength_boundaries(bits, label):
    assert generator.strength_label(bits) == label


def test_short_numeric_password_is_rated_weak():
    meta = json.loads(generator.generate_with_meta(8, False, False, True, False, False))
    assert meta["strength"] == "weak"


def test_generation_stays_well_under_50_ms():
    runs = 500
    started = time.perf_counter()
    for _ in range(runs):
        generator.generate_with_meta(32, True, True, True, True, False)
    mean_ms = (time.perf_counter() - started) * 1000.0 / runs
    assert mean_ms < 50
