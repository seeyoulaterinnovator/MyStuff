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

export interface ErrorBoundaryContextValue {
  error?: Error;
  domain?: ErrorBoundaryDomain;
  showBoundary: (error: Error, domain?: ErrorBoundaryDomain) => void;
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

  showBoundary = (error: Error, domain?: ErrorBoundaryDomain) => {
    this.setState({ error, domain });
  };

  render() {
    return (
      <ErrorBoundaryContext.Provider
        value={{
          error: this.state.error,
          domain: this.state.domain,
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
}

export interface ErrorBoundaryFallbackProps {
  fallback: ComponentType<FallbackProps>;
  children: ReactNode;
  domain?: ErrorBoundaryDomain;
}

export const ErrorBoundaryFallback: FunctionComponent<
  ErrorBoundaryFallbackProps
> = ({ children, fallback: FallbackComponent, domain }) => {
  const { error, domain: errorDomain } = useErrorBoundary();

  const isErrorBelongsDomain =
    errorDomain === domain ||
    ((!errorDomain || errorDomain === "page") && !domain);

  if (error && isErrorBelongsDomain) {
    console.error(error);
    return <FallbackComponent error={error} />;
  }

  return children;
};
