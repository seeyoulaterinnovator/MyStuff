import {
  Component,
  type ComponentType,
  type FunctionComponent,
  type GetDerivedStateFromError,
  type ReactNode,
} from "react";
import {
  createNamedContext,
  useRequiredContext,
} from "@keycloak/keycloak-ui-shared";

type ErrorBoundaryDomain = "page" | "context";

type ErrorBoundaryCause = "unauthenticated";

export interface ErrorBoundaryContextValue {
  error?: Error;
  domain?: ErrorBoundaryDomain;
  cause?: ErrorBoundaryCause;
  showBoundary: (
    error: Error,
    domain?: ErrorBoundaryDomain,
    cause?: ErrorBoundaryCause,
  ) => void;
}

const ErrorBoundaryContext = createNamedContext<
  ErrorBoundaryContextValue | undefined
>("ErrorBoundaryContext", undefined);

export const useErrorBoundary = () => useRequiredContext(ErrorBoundaryContext);

export interface ErrorBoundaryProviderProps {
  children: ReactNode;
}

export interface ErrorBoundaryProviderState {
  error?: Error;
  domain?: ErrorBoundaryDomain;
  cause?: ErrorBoundaryCause;
}

export class ErrorBoundaryProvider extends Component<
  ErrorBoundaryProviderProps,
  ErrorBoundaryProviderState
> {
  state: ErrorBoundaryProviderState = {};

  static getDerivedStateFromError: GetDerivedStateFromError<
    ErrorBoundaryProviderProps,
    ErrorBoundaryProviderState
  > = (error) => {
    return { error };
  };

  showBoundary = (
    error: Error,
    domain?: ErrorBoundaryDomain,
    cause?: ErrorBoundaryCause,
  ) => {
    this.setState({ error, domain, cause });
  };

  render() {
    return (
      <ErrorBoundaryContext.Provider
        value={{
          error: this.state.error,
          domain: this.state.domain,
          cause: this.state.cause,
          showBoundary: this.showBoundary,
        }}
      >
        {this.props.children}
      </ErrorBoundaryContext.Provider>
    );
  }
}

export interface FallbackProps {
  error: Error;
  withSignOut?: boolean;
  cause?: ErrorBoundaryCause;
}

export interface ErrorBoundaryFallbackProps {
  fallback: ComponentType<FallbackProps>;
  children: ReactNode;
  domain?: ErrorBoundaryDomain;
  cause?: ErrorBoundaryCause;
}

export const ErrorBoundaryFallback: FunctionComponent<
  ErrorBoundaryFallbackProps
> = ({ children, fallback: FallbackComponent, domain, cause }) => {
  const { error, domain: errorDomain, cause: errorCause } = useErrorBoundary();

  const isErrorBelongsDomain =
    errorDomain === domain ||
    ((!errorDomain || errorDomain === "page") && !domain);

  if (error && isErrorBelongsDomain) {
    console.error(error);
    return <FallbackComponent error={error} cause={cause || errorCause} />;
  }

  return children;
};
