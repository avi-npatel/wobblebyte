import pathlib
import sys

# The generator lives in the Android module's Python source set so Chaquopy
# bundles it into the APK. Tests import it from there.
PYTHON_SRC = pathlib.Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "python"
sys.path.insert(0, str(PYTHON_SRC))
