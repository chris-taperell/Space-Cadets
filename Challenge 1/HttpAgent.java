import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generalised agent to pull HTTP requests and do basic processing.
 */
public class HttpAgent {
  private String target;
  private List<String> output;
  private boolean pullSuccess;

  public HttpAgent(String trg) {
    // Initialise target but don't fetch until it's needed.
    target = trg;
  }

  /**
   * Private function called to fetch output. Lazy; does not usually fetch until required.
   * Only tries to fetch once, can be forced to fetch again using ForceFetchOutput().
   */
  private void FetchOutput() {
    List<String> out;

    try {
      // Open URL using BufferedReader
      URL url = new URL(target);
      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

      out = in.lines().collect(Collectors.toList());
    } catch (IOException e) {
      // If error in reader, returns with pullSuccess false (so GetOutput will fail)
      return;
    }

    output = out;
    pullSuccess = true;

    // Basic functionality to handle redirects
    // This string will be an error if not redirect
    // Need to manually upgrade to https because the site doesn't respond on http
    String possibleRedirect = GetOutput(
        5, "document has moved <a href=\"", "\">here</a>"
    ).replace("http://", "https://");

    // If it contains an ecs.soton link on this line, it's almost 100% chance to be a redirect
    if (possibleRedirect.contains("ecs.soton")) {
      // If the redirect exists, fetch again after updating target.
      System.out.println("(WARN) Redirected to " + possibleRedirect);
      target = possibleRedirect;
      ForceFetchOutput();
    }
  }

  /**
   * Forces the agent to fetch everything again. Public, unlike FetchOutput().
   * Usually not required unless the webpage changes constantly.
   * Clears the output list before re-fetching.
   */
  public void ForceFetchOutput() {
    pullSuccess = false;
    output.clear();

    FetchOutput();
  }

  /**
   * Returns an entire line from the fetched data.
   */
  public String GetOutput(int line) {
    // Pull if not pulled already
    if (!pullSuccess) {
      FetchOutput();
    }

    // Test if output exists and has been pulled
    if (pullSuccess && output.size() > 0) {
      // Make sure the requested line is within bounds
      if (line < output.size()) {
        return output.get(line);
      } else {
        return "(ERROR) Line index out of range.";
      }
    } else {
      return "(ERROR) No output. URL may be invalid.";
    }
  }

  /**
   * Gets given line then splits it by start and end, returning the inside result.
   */
  public String GetOutput(int line, String start, String end) {
    // Pull if not pulled already
    if (!pullSuccess) {
      FetchOutput();
    }

    // Test if output exists and has been pulled
    if (pullSuccess && output.size() > 0) {
      // Make sure the requested line is within bounds
      if (line < output.size()) {
        // Ensure both start and end exist in the requested line
        String lineGet = output.get(line);
        if (lineGet.contains(start) && lineGet.contains(end)) {
          return lineGet.split(start)[1].split(end)[0];
        }
        else {
          return "(ERROR) Start/end not found.";
        }
      } else {
        return "(ERROR) Line index out of range.";
      }
    } else {
      return "(ERROR) No output. URL may be invalid.";
    }
  }
}
