# Reviewer 1

*This is the first paper describing Tracer, the most frequently used software for convergence analysis of phylogenetic MCMC. Thus this paper certainly merits publication, and will no doubt get thousands of citations. Tracer was already very useful, and this new version brings additional useful functionality. The paper is mostly an announcement, and is well written though we have a few comments.*

Thanks

## Regarding the software:

*S1: How does the conditional posterior visualization work? That seems like a major improvement, but it’s not shown anywhere or described beyond a mention in the intro. The documentation basically says “try things out!”-- this mostly works except for here.*

** To do **

*S2: We were happy to see that the HME is no longer an option for model comparison (given its notorious bias).*

No response required

*S3: Box plots and violin plots are a fantastic improvement for viewing multiple distributions at the same time! The >2 dimensional correlation plots are also a welcome addition to the toolkit. *

No response required

*S4: We miss the ability to adjust the number of bins for histograms of univariate marginal distributions.*

This feature has been restored [Issue #146](https://github.com/beast-dev/tracer/issues/146).

*S5: Please update the link on http://beast.community/tracer , which currently links to 1.6 (even though it says it’s linking to 1.7)*

** To do **

* P1: People mostly use Tracer to diagnose convergence problems. I think the best use of a paper about Tracer would be to walk readers through a side-by-side comparison of two runs, one of which has converged and another that hasn’t. This is additional work, but I think it would be very valuable to guide the community in making such calls. At least, there should be a link in the paper to some material giving such an example. The current Figures 1 b-e and 2a-c feel a little generic-- we know what these plot types look like. *

** To do **

Details:

* Title: We cannot resist the temptation to suggest to the more active “Summarizing Bayesian phylogenetic posterior distributions using Tracer 1.7”*

*L68: the BSSVS description doesn’t really feel like it comes to a satisfactory conclusion. Can you more clearly describe how Tracer helps with this type of analysis? Is the sentence at line 80 a continuation of this thought, or starting something new?*

*L197: the proper citation is “...finds its rootS”*

*Fig 1a: the parameter name being open for editing is distracting*

*L108: I am not sure I understand the comparison (“model complexity”) being made between the marginal and joint marginal plots. If it is just the number of nonzero parameters, it could be stated more clearly.*

*L130: coda also offers diagnostics absent from Tracer, such as Gelman-Rubin statistics.*


Please cite RWTY https://academic.oup.com/mbe/article/34/4/1016/2900564 , which has some features (e.g. topology diagnostics) that are not present in Tracer.
Figure 3 has no axis labels of any kind
