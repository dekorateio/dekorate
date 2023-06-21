/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/

package io.dekorate.tekton.step;

import static io.dekorate.utils.Git.REMOTE_PATTERN;
import static io.dekorate.utils.Git.sanitizeRemoteUrl;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.dekorate.project.ScmInfo;
import io.dekorate.tekton.config.TektonConfig;
import io.dekorate.utils.Git;

public final class GitCloneStep implements Step {

  public static final String ID = "git-clone";

  public static final String REPO_URL_PARAM_NAME = "repoUrl";
  public static final String REPO_URL_PARAM_DESCRIPTION = "Repository URL to clone from.";
  public static final String REPO_URL_PARAM_REF = "$(inputs.params." + REPO_URL_PARAM_NAME + ")";

  public static final String REVISION_PARAM_NAME = "revision";
  public static final String REVISION_PARAM_DESCRIPTION = "Revision to checkout. (branch, tag, sha, ref, etc...)";
  public static final String REVISION_PARAM_REF = "$(inputs.params." + REVISION_PARAM_NAME + ")";

  public static final String IMAGE_PARAM_NAME = "gitCloneInitImage";
  public static final String IMAGE_PARAM_DESCRIPTION = "The image providing the git-init binary that this Task runs.";
  public static final String IMAGE_PARAM_REF = "$(inputs.params." + IMAGE_PARAM_NAME + ")";
  public static final String IMAGE_PARAM_DEFAULT_VALUE = "gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/git-init:v0.40.2";

  public static String getRepoUrl(TektonConfig config) {
    ScmInfo scm = Optional.ofNullable(config.getProject().getScmInfo())
        .orElseThrow(() -> new IllegalStateException("No scm info found!"));

    String repoUrl = null;
    if (scm.getRemote() != null) {
      // Try to find the Origin remote
      Pattern remotePattern = Pattern.compile(REMOTE_PATTERN);
      for (Map.Entry<String, String> remote : scm.getRemote().entrySet()) {
        Matcher m = remotePattern.matcher(remote.getKey());
        if (m.matches()) {
          String remoteValue = m.group(1);
          if (Git.ORIGIN.equals(remoteValue)) {
            repoUrl = remote.getValue();
            break;
          }
        }
      }

      // if not found, let's pick up the first remote
      if (repoUrl == null) {
        repoUrl = scm.getRemote().values().iterator().next();
      }
    }

    if (repoUrl == null) {
      throw new IllegalStateException("Could not find the repository URL from the scm info!");
    }

    return sanitizeRemoteUrl(repoUrl);
  }
}
